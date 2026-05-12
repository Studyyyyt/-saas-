package com.example.springboot.service;

import com.example.springboot.entity.LabBillTemplate;
import com.example.springboot.entity.LabFactory;
import com.example.springboot.entity.LabFactoryProduct;
import com.example.springboot.mapper.LabBillMapper;
import com.example.springboot.mapper.LabBillTemplateMapper;
import com.example.springboot.mapper.LabFactoryMapper;
import com.example.springboot.mapper.LabFactoryProductMapper;
import com.example.springboot.mapper.LabOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class LabFactoryService {

    public static final String FACTORY_STATUS_ACTIVE = "合作中";
    public static final String FACTORY_STATUS_INACTIVE = "已停止合作";
    public static final String PRODUCT_STATUS_ENABLED = "启用";
    public static final String PRODUCT_STATUS_DISABLED = "停用";

    private final LabFactoryMapper labFactoryMapper;
    private final LabFactoryProductMapper labFactoryProductMapper;
    private final LabBillTemplateMapper labBillTemplateMapper;
    private final LabOrderMapper labOrderMapper;
    private final LabBillMapper labBillMapper;

    @Autowired
    public LabFactoryService(LabFactoryMapper labFactoryMapper,
                             LabFactoryProductMapper labFactoryProductMapper,
                             LabBillTemplateMapper labBillTemplateMapper,
                             LabOrderMapper labOrderMapper,
                             LabBillMapper labBillMapper) {
        this.labFactoryMapper = labFactoryMapper;
        this.labFactoryProductMapper = labFactoryProductMapper;
        this.labBillTemplateMapper = labBillTemplateMapper;
        this.labOrderMapper = labOrderMapper;
        this.labBillMapper = labBillMapper;
    }

    public List<LabFactory> searchFactories(String keyword, String status) {
        String normalizedKeyword = normalizeText(keyword).toLowerCase(Locale.ROOT);
        String normalizedStatus = normalizeFactoryStatus(status, false);
        return labFactoryMapper.selectAll().stream()
                .filter(Objects::nonNull)
                .filter(item -> normalizedStatus.isEmpty() || normalizedStatus.equals(item.getStatus()))
                .filter(item -> normalizedKeyword.isEmpty() || containsFactoryKeyword(item, normalizedKeyword))
                .sorted(Comparator.comparing(LabFactory::getUpdated_at, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(LabFactory::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<LabFactory> selectEnabled() {
        return labFactoryMapper.selectEnabled();
    }

    public LabFactory selectById(Long id) {
        return id == null ? null : labFactoryMapper.selectById(id);
    }

    public List<LabFactoryProduct> selectProducts(Long factoryId, boolean onlyEnabled) {
        requireExistingFactory(factoryId);
        return onlyEnabled ? labFactoryProductMapper.selectEnabledByFactoryId(factoryId)
                : labFactoryProductMapper.selectByFactoryId(factoryId);
    }

    public List<LabBillTemplate> selectTemplates(Long factoryId) {
        requireExistingFactory(factoryId);
        return labBillTemplateMapper.selectByFactoryId(factoryId);
    }

    @Transactional
    public LabFactory addFactory(LabFactory item) {
        validateFactory(item);
        item.setStatus(normalizeFactoryStatus(item.getStatus(), true));
        item.setName(item.getName().trim());
        item.setContact_name(trimToNull(item.getContact_name()));
        item.setContact_phone(trimToNull(item.getContact_phone()));
        item.setAddress(trimToNull(item.getAddress()));
        labFactoryMapper.insert(item);
        return item;
    }

    @Transactional
    public LabFactory editFactory(LabFactory item) {
        if (item == null || item.getId() == null || item.getId() <= 0) {
            throw new IllegalArgumentException("加工厂ID不能为空");
        }
        requireExistingFactory(item.getId());
        validateFactory(item);
        item.setStatus(normalizeFactoryStatus(item.getStatus(), true));
        item.setName(item.getName().trim());
        item.setContact_name(trimToNull(item.getContact_name()));
        item.setContact_phone(trimToNull(item.getContact_phone()));
        item.setAddress(trimToNull(item.getAddress()));
        labFactoryMapper.update(item);
        return labFactoryMapper.selectById(item.getId());
    }

    @Transactional
    public void deleteFactory(Long id) {
        requireExistingFactory(id);
        boolean hasOrders = labOrderMapper.selectAll().stream().anyMatch(item -> item != null && id.equals(item.getFactory_id()));
        boolean hasBills = labBillMapper.selectAll().stream().anyMatch(item -> item != null && id.equals(item.getFactory_id()));
        if (hasOrders || hasBills) {
            throw new IllegalArgumentException("该加工厂已有订单或账单记录，不能删除");
        }
        labFactoryProductMapper.deleteByFactoryId(id);
        labBillTemplateMapper.deleteByFactoryId(id);
        labFactoryMapper.delete(id);
    }

    @Transactional
    public LabFactoryProduct addProduct(Long factoryId, LabFactoryProduct item) {
        requireExistingFactory(factoryId);
        validateProduct(item);
        item.setFactory_id(factoryId);
        item.setProduct_name(item.getProduct_name().trim());
        item.setProduct_spec(trimToNull(item.getProduct_spec()));
        item.setUnit(trimToNull(item.getUnit()));
        item.setStatus(normalizeProductStatus(item.getStatus(), true));
        item.setUnit_price(normalizeMoney(item.getUnit_price()));
        labFactoryProductMapper.insert(item);
        return item;
    }

    @Transactional
    public LabFactoryProduct editProduct(Long factoryId, LabFactoryProduct item) {
        requireExistingFactory(factoryId);
        if (item == null || item.getId() == null || item.getId() <= 0) {
            throw new IllegalArgumentException("价格表ID不能为空");
        }
        LabFactoryProduct existing = requireExistingProduct(item.getId());
        if (!factoryId.equals(existing.getFactory_id())) {
            throw new IllegalArgumentException("价格表不属于该加工厂");
        }
        validateProduct(item);
        item.setFactory_id(factoryId);
        item.setProduct_name(item.getProduct_name().trim());
        item.setProduct_spec(trimToNull(item.getProduct_spec()));
        item.setUnit(trimToNull(item.getUnit()));
        item.setStatus(normalizeProductStatus(item.getStatus(), true));
        item.setUnit_price(normalizeMoney(item.getUnit_price()));
        labFactoryProductMapper.update(item);
        return labFactoryProductMapper.selectById(item.getId());
    }

    @Transactional
    public void batchSaveProducts(Long factoryId, List<LabFactoryProduct> items) {
        requireExistingFactory(factoryId);
        if (items == null || items.isEmpty()) {
            return;
        }
        for (LabFactoryProduct item : items) {
            if (item == null) {
                continue;
            }
            if (item.getId() == null || item.getId() <= 0) {
                addProduct(factoryId, item);
            } else {
                editProduct(factoryId, item);
            }
        }
    }

    @Transactional
    public void deleteProduct(Long factoryId, Long productId) {
        requireExistingFactory(factoryId);
        LabFactoryProduct existing = requireExistingProduct(productId);
        if (!factoryId.equals(existing.getFactory_id())) {
            throw new IllegalArgumentException("价格表不属于该加工厂");
        }
        labFactoryProductMapper.delete(productId);
    }

    @Transactional
    public LabBillTemplate addTemplate(Long factoryId, LabBillTemplate item) {
        requireExistingFactory(factoryId);
        validateTemplate(item);
        item.setFactory_id(factoryId);
        item.setTemplate_name(item.getTemplate_name().trim());
        item.setColumn_mapping(item.getColumn_mapping().trim());
        item.setHeader_row(normalizePositiveInt(item.getHeader_row(), 1));
        item.setData_start_row(normalizePositiveInt(item.getData_start_row(), 2));
        labBillTemplateMapper.insert(item);
        return item;
    }

    @Transactional
    public LabBillTemplate editTemplate(Long factoryId, LabBillTemplate item) {
        requireExistingFactory(factoryId);
        if (item == null || item.getId() == null || item.getId() <= 0) {
            throw new IllegalArgumentException("模板ID不能为空");
        }
        LabBillTemplate existing = requireExistingTemplate(item.getId());
        if (!factoryId.equals(existing.getFactory_id())) {
            throw new IllegalArgumentException("模板不属于该加工厂");
        }
        validateTemplate(item);
        item.setFactory_id(factoryId);
        item.setTemplate_name(item.getTemplate_name().trim());
        item.setColumn_mapping(item.getColumn_mapping().trim());
        item.setHeader_row(normalizePositiveInt(item.getHeader_row(), 1));
        item.setData_start_row(normalizePositiveInt(item.getData_start_row(), 2));
        labBillTemplateMapper.update(item);
        return labBillTemplateMapper.selectById(item.getId());
    }

    @Transactional
    public void deleteTemplate(Long factoryId, Long templateId) {
        requireExistingFactory(factoryId);
        LabBillTemplate existing = requireExistingTemplate(templateId);
        if (!factoryId.equals(existing.getFactory_id())) {
            throw new IllegalArgumentException("模板不属于该加工厂");
        }
        labBillTemplateMapper.delete(templateId);
    }

    private boolean containsFactoryKeyword(LabFactory item, String keyword) {
        return normalizeText(item.getName()).toLowerCase(Locale.ROOT).contains(keyword)
                || normalizeText(item.getContact_name()).toLowerCase(Locale.ROOT).contains(keyword)
                || normalizeText(item.getContact_phone()).toLowerCase(Locale.ROOT).contains(keyword)
                || normalizeText(item.getAddress()).toLowerCase(Locale.ROOT).contains(keyword);
    }

    private void validateFactory(LabFactory item) {
        if (item == null) {
            throw new IllegalArgumentException("加工厂信息不能为空");
        }
        if (!StringUtils.hasText(item.getName())) {
            throw new IllegalArgumentException("加工厂名称不能为空");
        }
        normalizeFactoryStatus(item.getStatus(), true);
    }

    private void validateProduct(LabFactoryProduct item) {
        if (item == null) {
            throw new IllegalArgumentException("价格表信息不能为空");
        }
        if (!StringUtils.hasText(item.getProduct_name())) {
            throw new IllegalArgumentException("产品名称不能为空");
        }
        if (normalizeMoney(item.getUnit_price()).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("单价不能小于0");
        }
        normalizeProductStatus(item.getStatus(), true);
    }

    private void validateTemplate(LabBillTemplate item) {
        if (item == null) {
            throw new IllegalArgumentException("模板信息不能为空");
        }
        if (!StringUtils.hasText(item.getTemplate_name())) {
            throw new IllegalArgumentException("模板名称不能为空");
        }
        if (!StringUtils.hasText(item.getColumn_mapping())) {
            throw new IllegalArgumentException("列映射不能为空");
        }
        if (normalizePositiveInt(item.getHeader_row(), 1) <= 0) {
            throw new IllegalArgumentException("表头行号必须大于0");
        }
        if (normalizePositiveInt(item.getData_start_row(), 2) <= 0) {
            throw new IllegalArgumentException("数据起始行号必须大于0");
        }
    }

    private LabFactory requireExistingFactory(Long factoryId) {
        if (factoryId == null || factoryId <= 0) {
            throw new IllegalArgumentException("加工厂ID不能为空");
        }
        LabFactory factory = labFactoryMapper.selectById(factoryId);
        if (factory == null) {
            throw new IllegalArgumentException("加工厂不存在");
        }
        return factory;
    }

    private LabFactoryProduct requireExistingProduct(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("价格表ID不能为空");
        }
        LabFactoryProduct item = labFactoryProductMapper.selectById(productId);
        if (item == null) {
            throw new IllegalArgumentException("价格表不存在");
        }
        return item;
    }

    private LabBillTemplate requireExistingTemplate(Long templateId) {
        if (templateId == null || templateId <= 0) {
            throw new IllegalArgumentException("模板ID不能为空");
        }
        LabBillTemplate item = labBillTemplateMapper.selectById(templateId);
        if (item == null) {
            throw new IllegalArgumentException("账单模板不存在");
        }
        return item;
    }

    private String normalizeFactoryStatus(String status, boolean applyDefault) {
        String normalized = normalizeText(status);
        if (normalized.isEmpty() && applyDefault) {
            return FACTORY_STATUS_ACTIVE;
        }
        if (normalized.isEmpty()) {
            return "";
        }
        if (!FACTORY_STATUS_ACTIVE.equals(normalized) && !FACTORY_STATUS_INACTIVE.equals(normalized)) {
            throw new IllegalArgumentException("加工厂状态不合法");
        }
        return normalized;
    }

    private String normalizeProductStatus(String status, boolean applyDefault) {
        String normalized = normalizeText(status);
        if (normalized.isEmpty() && applyDefault) {
            return PRODUCT_STATUS_ENABLED;
        }
        if (normalized.isEmpty()) {
            return "";
        }
        if (!PRODUCT_STATUS_ENABLED.equals(normalized) && !PRODUCT_STATUS_DISABLED.equals(normalized)) {
            throw new IllegalArgumentException("价格表状态不合法");
        }
        return normalized;
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private int normalizePositiveInt(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    public Map<String, Object> buildOverview() {
        List<LabFactory> all = labFactoryMapper.selectAll();
        int activeCount = 0;
        int inactiveCount = 0;
        for (LabFactory f : all) {
            if (f == null) continue;
            if (FACTORY_STATUS_ACTIVE.equals(f.getStatus())) activeCount++;
            else if (FACTORY_STATUS_INACTIVE.equals(f.getStatus())) inactiveCount++;
        }
        int totalProducts = labFactoryProductMapper.selectAll().size();
        int totalTemplates = labBillTemplateMapper.selectAll().size();

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("total_count", all.size());
        result.put("active_count", activeCount);
        result.put("inactive_count", inactiveCount);
        result.put("total_products", totalProducts);
        result.put("total_templates", totalTemplates);
        return result;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
