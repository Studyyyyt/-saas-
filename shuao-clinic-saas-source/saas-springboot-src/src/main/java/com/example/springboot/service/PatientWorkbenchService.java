package com.example.springboot.service;

import com.example.springboot.entity.Account;
import com.example.springboot.entity.PatientWorkbenchBaseRow;
import com.example.springboot.entity.PatientCustomGroup;
import com.example.springboot.entity.PatientRiskTag;
import com.example.springboot.entity.PatientWorkbenchBuiltinCounts;
import com.example.springboot.entity.PatientWorkbenchDoctorOption;
import com.example.springboot.entity.PatientWorkbenchGroupCount;
import com.example.springboot.entity.PatientWorkbenchQuery;
import com.example.springboot.entity.PatientWorkbenchRow;
import com.example.springboot.entity.PatientWorkbenchTag;
import com.example.springboot.mapper.PatientWorkbenchMapper;
import com.example.springboot.util.PatientSearchUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PatientWorkbenchService {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 5000;
    private static final List<String> GROUP_KEYS = List.of(
            "all", "recent", "public", "implant", "rootCanal", "ortho", "repair",
            "cleaning", "removable", "extraction", "filling", "periodontal",
            "highValue", "lostRisk", "wordOfMouth",
            "unconverted", "other"
    );
    private static final Map<String, String> GROUP_LABELS = createGroupLabels();

    private final PatientWorkbenchMapper patientWorkbenchMapper;
    private final PatientRiskTagService patientRiskTagService;
    private final PatientCustomGroupService patientCustomGroupService;
    private final AccountService accountService;
    private final TreatmentBillingService treatmentBillingService;

    @Autowired
    public PatientWorkbenchService(PatientWorkbenchMapper patientWorkbenchMapper,
                                   PatientRiskTagService patientRiskTagService,
                                   PatientCustomGroupService patientCustomGroupService,
                                   AccountService accountService,
                                   TreatmentBillingService treatmentBillingService) {
        this.patientWorkbenchMapper = patientWorkbenchMapper;
        this.patientRiskTagService = patientRiskTagService;
        this.patientCustomGroupService = patientCustomGroupService;
        this.accountService = accountService;
        this.treatmentBillingService = treatmentBillingService;
    }

    public Map<String, Object> search(PatientWorkbenchQuery rawQuery) {
        PatientWorkbenchQuery query = normalizeQuery(rawQuery);
        List<PatientCustomGroup> customGroups = safeList(patientCustomGroupService.selectActive());
        query.setGroupKey(resolveGroupKey(query.getGroupKey(), customGroups));
        List<Account> activeDoctors = safeList(accountService.findActiveDoctorAccounts());
        Map<Long, String> doctorNameById = buildDoctorNameById(activeDoctors);

        PatientWorkbenchQuery summaryQuery = copyQuery(query);
        summaryQuery.setGroupKey("all");

        List<PatientWorkbenchBaseRow> pagedBaseRows;
        long total;
        Map<String, Integer> groupCounts;
        Map<String, Object> filterOptions;

        PageHelper.startPage(query.getPage(), query.getSize());
        pagedBaseRows = safeList(patientWorkbenchMapper.selectBaseRows(query));
        total = new PageInfo<>(pagedBaseRows).getTotal();
        PageHelper.clearPage();
        PatientWorkbenchBuiltinCounts builtinCounts = patientWorkbenchMapper.selectBuiltinGroupCounts(summaryQuery);
        List<PatientWorkbenchGroupCount> customGroupCounts = safeList(patientWorkbenchMapper.selectCustomGroupCounts(summaryQuery));
        List<PatientWorkbenchDoctorOption> doctorRows = safeList(patientWorkbenchMapper.selectDoctorOptions(summaryQuery));
        List<String> sourceRows = safeList(patientWorkbenchMapper.selectSourceOptions(summaryQuery));
        List<String> relationRows = safeList(patientWorkbenchMapper.selectRelationOptions(summaryQuery));
        groupCounts = buildGroupCounts(builtinCounts, customGroupCounts, customGroups);
        filterOptions = buildFilterOptions(doctorRows, sourceRows, relationRows, activeDoctors);

        List<Long> pagedPatientIds = pagedBaseRows.stream()
                .filter(Objects::nonNull)
                .map(item -> (long) item.getId())
                .filter(id -> id > 0)
                .toList();
        Map<Long, List<PatientRiskTag>> riskTagsByPatientId = safeList(patientRiskTagService.selectActiveByPatientIds(pagedPatientIds)).stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getPatient_id() != null && item.getPatient_id() > 0)
                .collect(Collectors.groupingBy(PatientRiskTag::getPatient_id, LinkedHashMap::new, Collectors.toList()));
        List<PatientWorkbenchRow> pageRows = pagedBaseRows.stream()
                .filter(Objects::nonNull)
                .map(row -> hydrateBaseRow(
                        prepareSummaryRow(row, doctorNameById),
                        riskTagsByPatientId.getOrDefault((long) row.getId(), List.of())
                ))
                .toList();

        int safePage = total == 0 ? 1 : Math.min(query.getPage(), (int) Math.ceil(total / (double) query.getSize()));
        Map<String, Object> result = new LinkedHashMap<>(buildPagedResult(pageRows, total, safePage, query.getSize()));
        result.put("groupCounts", groupCounts);
        result.put("customGroups", buildCustomGroups(customGroups));
        result.put("filterOptions", filterOptions);
        result.put("capabilities", buildCapabilities());
        return result;
    }

    private PatientWorkbenchBaseRow prepareSummaryRow(PatientWorkbenchBaseRow row,
                                                      Map<Long, String> doctorNameById) {
        row.setLatest_visit_doctor_account_id(normalizePositiveId(row.getLatest_visit_doctor_account_id()));
        row.setLatest_visit_doctor_name(trimToNull(resolveDoctorName(row.getLatest_visit_doctor_account_id(), row.getLatest_visit_doctor_name(), doctorNameById)));
        row.setLatest_visit_source(trimToNull(row.getLatest_visit_source()));
        row.setLatest_visit_doctor(row.getLatest_visit_doctor_name());
        row.setFollowup_doctor_account_id(normalizePositiveId(row.getFollowup_doctor_account_id()));
        row.setFollowup_doctor_name(trimToNull(resolveDoctorName(row.getFollowup_doctor_account_id(), row.getFollowup_doctor_name(), doctorNameById)));
        row.setNext_followup_overdue(row.getNext_followup_date() != null && row.getNext_followup_date().before(new Date()));
        double arrearsAmount = row.getArrears_amount() == null ? 0D : row.getArrears_amount();
        row.setHas_arrears(arrearsAmount > 0.0001);
        row.setArrears_amount(round2(arrearsAmount));
        row.setTotal_spent(round2(row.getTotal_spent() == null ? 0D : row.getTotal_spent()));
        row.setVisit_count(row.getVisit_count() == null ? 0 : row.getVisit_count());
        row.setVisit_count_last_6m(row.getVisit_count_last_6m() == null ? 0 : row.getVisit_count_last_6m());
        row.setReferred_count(row.getReferred_count() == null ? 0 : row.getReferred_count());
        row.setReferred_revenue(round2(row.getReferred_revenue() == null ? 0D : row.getReferred_revenue()));
        row.setCustom_group_keys(parseCustomGroupKeys(row.getCustom_group_keys_text()));
        GroupResolution groupResolution = resolveGroupResolutionFromSignal(row.getGroup_signal_text());
        row.setGroup_keys(groupResolution.groupKeys);
        row.setPrimary_group_key(groupResolution.primaryGroupKey);
        row.setPrimary_group_label(GROUP_LABELS.getOrDefault(groupResolution.primaryGroupKey, ""));
        return row;
    }

    private PatientWorkbenchRow hydrateBaseRow(PatientWorkbenchBaseRow row,
                                               List<PatientRiskTag> riskTags) {
        List<PatientRiskTag> activeRiskTags = safeList(riskTags).stream()
                .filter(this::isActiveRiskTag)
                .sorted(Comparator.comparing(PatientRiskTag::getRisk_level, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(PatientRiskTag::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        row.setRisk_tags(activeRiskTags);
        row.setPatient_tags(buildTags(row, activeRiskTags));
        return row;
    }

    private GroupResolution resolveGroupResolutionFromSignal(String signalText) {
        LinkedHashSet<String> groupKeys = new LinkedHashSet<>();
        boolean hasSignal = StringUtils.hasText(trimToNull(signalText));
        if (hasSignal) {
            for (String key : GROUP_KEYS) {
                if ("all".equals(key) || "recent".equals(key) || "public".equals(key) || "unconverted".equals(key) || "other".equals(key)) {
                    continue;
                }
                if (matchesSignalGroup(signalText, key)) {
                    groupKeys.add(key);
                }
            }
        }
        String primaryGroupKey = groupKeys.stream().findFirst().orElse("");
        if (!StringUtils.hasText(primaryGroupKey) && hasSignal) {
            primaryGroupKey = "other";
        }
        return new GroupResolution(new ArrayList<>(groupKeys), primaryGroupKey);
    }

    private List<PatientWorkbenchTag> buildTags(PatientWorkbenchRow row, List<PatientRiskTag> riskTags) {
        List<PatientWorkbenchTag> tags = new ArrayList<>();
        if (StringUtils.hasText(row.getCustomer_source())) {
            tags.add(new PatientWorkbenchTag(row.getCustomer_source().trim(), "info", "source"));
        }
        if (StringUtils.hasText(row.getRelation_type())) {
            tags.add(new PatientWorkbenchTag(row.getRelation_type().trim(), "success", "relation"));
        }
        if (row.getNext_followup_date() != null) {
            String text = "复诊 " + formatMonthDay(row.getNext_followup_date());
            String type = Boolean.TRUE.equals(row.getNext_followup_overdue()) ? "danger" : "";
            tags.add(new PatientWorkbenchTag(text, type, "followup"));
        }
        if (Boolean.TRUE.equals(row.getHas_arrears())) {
            tags.add(new PatientWorkbenchTag("欠费 ¥" + formatMoney(row.getArrears_amount()), "warning", "arrears"));
        }
        if (Boolean.TRUE.equals(row.getHigh_value_flag())) {
            tags.add(new PatientWorkbenchTag("高价值", "success", "insight"));
        }
        if (Boolean.TRUE.equals(row.getLost_risk_flag())) {
            tags.add(new PatientWorkbenchTag("流失风险", "danger", "insight"));
        }
        if (Boolean.TRUE.equals(row.getWord_of_mouth_flag())) {
            tags.add(new PatientWorkbenchTag("口碑客户", "warning", "insight"));
        }
        for (PatientRiskTag riskTag : safeList(riskTags)) {
            if (!StringUtils.hasText(riskTag.getTag_name())) {
                continue;
            }
            tags.add(new PatientWorkbenchTag(riskTag.getTag_name().trim(), riskTagType(riskTag.getRisk_level()), "risk"));
        }
        return tags.stream().limit(5).toList();
    }

    private Map<String, Integer> buildGroupCounts(PatientWorkbenchBuiltinCounts builtinCounts,
                                                  List<PatientWorkbenchGroupCount> customCounts,
                                                  List<PatientCustomGroup> customGroups) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("all", toInt(builtinCounts == null ? null : builtinCounts.getAll_count()));
        counts.put("recent", toInt(builtinCounts == null ? null : builtinCounts.getRecent_count()));
        counts.put("public", toInt(builtinCounts == null ? null : builtinCounts.getPublic_count()));
        counts.put("implant", toInt(builtinCounts == null ? null : builtinCounts.getImplant_count()));
        counts.put("rootCanal", toInt(builtinCounts == null ? null : builtinCounts.getRootCanal_count()));
        counts.put("ortho", toInt(builtinCounts == null ? null : builtinCounts.getOrtho_count()));
        counts.put("repair", toInt(builtinCounts == null ? null : builtinCounts.getRepair_count()));
        counts.put("cleaning", toInt(builtinCounts == null ? null : builtinCounts.getCleaning_count()));
        counts.put("removable", toInt(builtinCounts == null ? null : builtinCounts.getRemovable_count()));
        counts.put("extraction", toInt(builtinCounts == null ? null : builtinCounts.getExtraction_count()));
        counts.put("filling", toInt(builtinCounts == null ? null : builtinCounts.getFilling_count()));
        counts.put("periodontal", toInt(builtinCounts == null ? null : builtinCounts.getPeriodontal_count()));
        counts.put("highValue", toInt(builtinCounts == null ? null : builtinCounts.getHighValue_count()));
        counts.put("lostRisk", toInt(builtinCounts == null ? null : builtinCounts.getLostRisk_count()));
        counts.put("wordOfMouth", toInt(builtinCounts == null ? null : builtinCounts.getWordOfMouth_count()));
        counts.put("unconverted", toInt(builtinCounts == null ? null : builtinCounts.getUnconverted_count()));
        counts.put("other", toInt(builtinCounts == null ? null : builtinCounts.getOther_count()));
        Map<String, Integer> customCountMap = new LinkedHashMap<>();
        for (PatientWorkbenchGroupCount item : safeList(customCounts)) {
            if (item == null || !StringUtils.hasText(item.getGroup_key())) {
                continue;
            }
            customCountMap.put(item.getGroup_key().trim(), toInt(item.getTotal_count()));
        }
        for (PatientCustomGroup group : safeList(customGroups)) {
            if (group == null || !StringUtils.hasText(group.getGroup_key())) {
                continue;
            }
            String key = group.getGroup_key().trim();
            counts.put(key, customCountMap.getOrDefault(key, 0));
        }
        return counts;
    }

    private List<Map<String, Object>> buildCustomGroups(List<PatientCustomGroup> customGroups) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (PatientCustomGroup group : safeList(customGroups)) {
            if (group == null || !StringUtils.hasText(group.getGroup_key()) || !StringUtils.hasText(group.getGroup_name())) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", group.getId());
            item.put("key", group.getGroup_key().trim());
            item.put("label", group.getGroup_name().trim());
            item.put("group_name", group.getGroup_name().trim());
            item.put("sort_order", group.getSort_order());
            result.add(item);
        }
        return result;
    }

    private Map<String, Object> buildFilterOptions(List<PatientWorkbenchDoctorOption> doctorRows,
                                                   List<String> sourceRows,
                                                   List<String> relationRows,
                                                   List<Account> activeDoctors) {
        Map<String, Object> result = new LinkedHashMap<>();
        LinkedHashMap<String, Map<String, Object>> doctorOptions = new LinkedHashMap<>();
        for (Account doctor : safeList(activeDoctors)) {
            if (doctor == null || doctor.getId() <= 0 || !StringUtils.hasText(doctor.getName())) {
                continue;
            }
            String value = "id:" + doctor.getId();
            Map<String, Object> option = new LinkedHashMap<>();
            option.put("value", value);
            option.put("label", doctor.getName().trim());
            option.put("doctor_account_id", doctor.getId());
            option.put("doctor_name", doctor.getName().trim());
            doctorOptions.putIfAbsent(value, option);
        }
        for (PatientWorkbenchDoctorOption doctorRow : safeList(doctorRows)) {
            if (doctorRow == null) {
                continue;
            }
            Long doctorAccountId = normalizePositiveId(doctorRow.getDoctor_account_id());
            String doctorName = trimToNull(doctorRow.getDoctor_name());
            if (!StringUtils.hasText(doctorName)) {
                continue;
            }
            String value = doctorAccountId != null ? "id:" + doctorAccountId : "name:" + doctorName;
            Map<String, Object> option = new LinkedHashMap<>();
            option.put("value", value);
            option.put("label", doctorName);
            option.put("doctor_account_id", doctorAccountId);
            option.put("doctor_name", doctorName);
            doctorOptions.putIfAbsent(value, option);
        }
        LinkedHashSet<String> sourceOptions = new LinkedHashSet<>();
        for (String row : safeList(sourceRows)) {
            String value = trimToNull(row);
            if (value != null) {
                sourceOptions.add(value);
            }
        }
        LinkedHashSet<String> relationOptions = new LinkedHashSet<>();
        for (String row : safeList(relationRows)) {
            String value = trimToNull(row);
            if (value != null) {
                relationOptions.add(value);
            }
        }
        result.put("doctors", new ArrayList<>(doctorOptions.values()));
        result.put("sources", new ArrayList<>(sourceOptions));
        result.put("relations", new ArrayList<>(relationOptions));
        return result;
    }

    private Map<String, Object> buildCapabilities() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("serverPaging", true);
        result.put("serverFiltering", true);
        result.put("structuredGrouping", true);
        result.put("doctorScopeSplit", true);
        result.put("riskTags", true);
        result.put("nextFollowupDate", true);
        result.put("patientInsights", true);
        return result;
    }

    private boolean matchesGroup(PatientWorkbenchRow row, String groupKey) {
        if (!StringUtils.hasText(groupKey) || "all".equals(groupKey)) {
            return true;
        }
        if (row.getCustom_group_keys() != null && row.getCustom_group_keys().contains(groupKey)) {
            return true;
        }
        switch (groupKey) {
            case "recent":
                Date recentDate = row.getLast_activity_at();
                if (recentDate == null) {
                    recentDate = firstNonNull(row.getUpdated_at(), row.getCreated_at());
                }
                if (recentDate == null) {
                    return false;
                }
                return !recentDate.toInstant().atZone(ZONE_ID).toLocalDate().isBefore(LocalDate.now(ZONE_ID).minusDays(30));
            case "public":
                return !StringUtils.hasText(trimToNull(firstNonBlank(row.getLatest_visit_doctor_name(), row.getLatest_visit_doctor())))
                        && row.getVisit_count() != null
                        && row.getVisit_count() <= 0
                        && !StringUtils.hasText(trimToNull(row.getLatest_treatment()))
                        && row.getNext_followup_date() == null;
            case "unconverted":
                return !StringUtils.hasText(trimToNull(row.getLatest_treatment()))
                        && (row.getVisit_count() == null || row.getVisit_count() <= 0);
            case "highValue":
                return Boolean.TRUE.equals(row.getHigh_value_flag());
            case "lostRisk":
                return Boolean.TRUE.equals(row.getLost_risk_flag());
            case "wordOfMouth":
                return Boolean.TRUE.equals(row.getWord_of_mouth_flag());
            case "other":
                return (row.getGroup_keys() == null || row.getGroup_keys().isEmpty())
                        && (row.getLast_visit_date() != null || StringUtils.hasText(trimToNull(row.getLatest_treatment())));
            default:
                return row.getGroup_keys() != null && row.getGroup_keys().contains(groupKey);
        }
    }

    private boolean matchesSignalGroup(String sourceText, String key) {
        String text = trimToNull(sourceText);
        if (!StringUtils.hasText(text)) {
            return false;
        }
        switch (key) {
            case "implant":
                return text.matches(".*(种植|植骨|上颌窦|基台).*");
            case "rootCanal":
                return text.matches(".*(根管|牙髓).*");
            case "ortho":
                return text.matches(".*(正畸|矫治|托槽|保持器).*");
            case "repair":
                return text.matches(".*(修复|全瓷|贴面|牙冠|烤瓷|嵌体|冠桥).*");
            case "cleaning":
                return text.matches(".*(洁治|洗牙|牙洁|龈上洁治).*");
            case "removable":
                return text.matches(".*(活动|义齿|吸附|胶托).*");
            case "extraction":
                return text.matches(".*(拔牙|阻生牙|拔除).*");
            case "filling":
                return text.matches(".*(补牙|充填|树脂).*");
            case "periodontal":
                return text.matches(".*(牙周|冠周|龈下刮治|龈瓣).*");
            default:
                return false;
        }
    }

    private PatientWorkbenchQuery normalizeQuery(PatientWorkbenchQuery rawQuery) {
        PatientWorkbenchQuery query = rawQuery == null ? new PatientWorkbenchQuery() : rawQuery;
        query.setPage(normalizePage(query.getPage()));
        query.setSize(normalizeSize(query.getSize()));

        String searchType = trimToNull(query.getSearchType());
        query.setSearchType("id".equals(searchType) ? "id" : "name");
        query.setKeyword(trimToNull(query.getKeyword()));

        String quickScope = trimToNull(query.getQuickScope());
        query.setQuickScope(isOneOf(quickScope, "today", "all", "recent") ? quickScope : "all");

        String groupKey = trimToNull(query.getGroupKey());
        query.setGroupKey(groupKey == null ? "all" : groupKey);

        query.setDoctorFilter(trimToNull(query.getDoctorFilter()));
        query.setSourceFilter(trimToNull(query.getSourceFilter()));
        query.setRelationFilter(trimToNull(query.getRelationFilter()));
        DoctorFilterValue doctorFilterValue = parseDoctorFilter(query.getDoctorFilter());
        query.setDoctorAccountId(doctorFilterValue.doctorAccountId);
        query.setDoctorName(doctorFilterValue.doctorName);

        String arrearsFilter = trimToNull(query.getArrearsFilter());
        query.setArrearsFilter(isOneOf(arrearsFilter, "arrears", "normal") ? arrearsFilter : "");
        String sortMode = trimToNull(query.getSortMode());
        query.setSortMode(isOneOf(sortMode, "recent", "idDesc", "totalSpentDesc", "visitCountDesc", "lastVisitDesc") ? sortMode : "idDesc");
        query.setKeyword(StringUtils.hasText(query.getKeyword()) ? PatientSearchUtils.normalizeKeyword(query.getKeyword()) : null);
        return query;
    }

    private PatientWorkbenchQuery copyQuery(PatientWorkbenchQuery source) {
        PatientWorkbenchQuery target = new PatientWorkbenchQuery();
        if (source == null) {
            return target;
        }
        target.setPage(source.getPage());
        target.setSize(source.getSize());
        target.setSearchType(source.getSearchType());
        target.setKeyword(source.getKeyword());
        target.setQuickScope(source.getQuickScope());
        target.setGroupKey(source.getGroupKey());
        target.setDoctorFilter(source.getDoctorFilter());
        target.setSourceFilter(source.getSourceFilter());
        target.setRelationFilter(source.getRelationFilter());
        target.setArrearsFilter(source.getArrearsFilter());
        target.setSortMode(source.getSortMode());
        target.setDoctorAccountId(source.getDoctorAccountId());
        target.setDoctorName(source.getDoctorName());
        return target;
    }

    private String resolveGroupKey(String groupKey, List<PatientCustomGroup> customGroups) {
        if (GROUP_LABELS.containsKey(groupKey)) {
            return groupKey;
        }
        for (PatientCustomGroup group : safeList(customGroups)) {
            if (group != null && StringUtils.hasText(group.getGroup_key()) && groupKey != null
                    && groupKey.equals(group.getGroup_key().trim())) {
                return groupKey;
            }
        }
        return "all";
    }

    private int normalizePage(Integer page) {
        return page == null || page <= 0 ? DEFAULT_PAGE : page;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private boolean isActiveRiskTag(PatientRiskTag riskTag) {
        return riskTag != null && riskTag.getPatient_id() != null && riskTag.getPatient_id() > 0
                && Objects.equals(riskTag.getStatus(), 1);
    }

    private Map<Long, String> buildDoctorNameById(List<Account> doctors) {
        Map<Long, String> result = new LinkedHashMap<>();
        for (Account doctor : safeList(doctors)) {
            if (doctor == null || doctor.getId() <= 0 || !StringUtils.hasText(doctor.getName())) {
                continue;
            }
            result.put((long) doctor.getId(), doctor.getName().trim());
        }
        return result;
    }

    private String resolveDoctorName(Long doctorAccountId, String fallbackDoctorName, Map<Long, String> doctorNameById) {
        Long normalizedId = normalizePositiveId(doctorAccountId);
        if (normalizedId != null) {
            String doctorName = doctorNameById == null ? null : doctorNameById.get(normalizedId);
            if (!StringUtils.hasText(doctorName)) {
                doctorName = accountService.findDoctorDisplayNameByAccountId(normalizedId);
            }
            if (StringUtils.hasText(doctorName)) {
                return doctorName.trim();
            }
        }
        return trimToNull(fallbackDoctorName);
    }

    private String formatMoney(Double value) {
        double amount = value == null ? 0D : value;
        return String.format(Locale.ROOT, "%.2f", amount);
    }

    private double round2(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private String formatMonthDay(Date value) {
        if (value == null) {
            return "";
        }
        return new SimpleDateFormat("MM-dd").format(value);
    }

    private String riskTagType(Integer riskLevel) {
        if (riskLevel == null) {
            return "info";
        }
        if (riskLevel >= 3) {
            return "danger";
        }
        if (riskLevel >= 2) {
            return "warning";
        }
        return "info";
    }

    private Long normalizePositiveId(Long value) {
        return value != null && value > 0 ? value : null;
    }

    private int toInt(Long value) {
        if (value == null) {
            return 0;
        }
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return value.intValue();
    }

    private List<String> parseCustomGroupKeys(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String item : value.split(",")) {
            String normalized = trimToNull(item);
            if (normalized != null) {
                result.add(normalized);
            }
        }
        return result;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean isOneOf(String value, String... options) {
        if (!StringUtils.hasText(value) || options == null) {
            return false;
        }
        for (String option : options) {
            if (value.equals(option)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> buildPagedResult(List<PatientWorkbenchRow> rows, long total, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int pages = total == 0 ? 0 : (int) Math.ceil(total / (double) safeSize);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("list", rows == null ? List.of() : rows);
        result.put("pageNum", safePage);
        result.put("pageSize", safeSize);
        result.put("size", rows == null ? 0 : rows.size());
        result.put("pages", pages);
        result.put("isFirstPage", safePage <= 1);
        result.put("isLastPage", pages == 0 || safePage >= pages);
        result.put("hasPreviousPage", safePage > 1);
        result.put("hasNextPage", safePage < pages);
        return result;
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }

    private static Map<String, String> createGroupLabels() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("all", "全部");
        result.put("recent", "最近患者");
        result.put("public", "公海患者");
        result.put("implant", "种植");
        result.put("rootCanal", "根管治疗");
        result.put("ortho", "正畸");
        result.put("repair", "修复");
        result.put("cleaning", "洁治");
        result.put("removable", "活动修复");
        result.put("extraction", "拔牙");
        result.put("filling", "补牙");
        result.put("periodontal", "牙周");
        result.put("highValue", "高价值客户");
        result.put("lostRisk", "流失风险");
        result.put("wordOfMouth", "口碑客户");
        result.put("unconverted", "未成交");
        result.put("other", "其他");
        return result;
    }

    private static final class GroupResolution {
        private final List<String> groupKeys;
        private final String primaryGroupKey;

        private GroupResolution(List<String> groupKeys, String primaryGroupKey) {
            this.groupKeys = groupKeys;
            this.primaryGroupKey = primaryGroupKey;
        }
    }

    private static final class DoctorFilterValue {
        private final Long doctorAccountId;
        private final String doctorName;

        private DoctorFilterValue(Long doctorAccountId, String doctorName) {
            this.doctorAccountId = doctorAccountId;
            this.doctorName = doctorName;
        }
    }

    private DoctorFilterValue parseDoctorFilter(String value) {
        String normalized = trimToNull(value);
        if (!StringUtils.hasText(normalized)) {
            return new DoctorFilterValue(null, null);
        }
        if (normalized.startsWith("id:")) {
            try {
                return new DoctorFilterValue(Long.parseLong(normalized.substring(3)), null);
            } catch (NumberFormatException ignored) {
                return new DoctorFilterValue(null, null);
            }
        }
        if (normalized.startsWith("name:")) {
            return new DoctorFilterValue(null, trimToNull(normalized.substring(5)));
        }
        return new DoctorFilterValue(null, normalized);
    }
}
