package com.example.springboot.mapper;

import com.example.springboot.entity.PatientWorkbenchBaseRow;
import com.example.springboot.entity.PatientWorkbenchBuiltinCounts;
import com.example.springboot.entity.PatientWorkbenchDoctorOption;
import com.example.springboot.entity.PatientWorkbenchGroupCount;
import com.example.springboot.entity.PatientWorkbenchQuery;
import com.example.springboot.mapper.provider.PatientWorkbenchSqlProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

@Mapper
public interface PatientWorkbenchMapper {

    @SelectProvider(type = PatientWorkbenchSqlProvider.class, method = "selectBaseRows")
    List<PatientWorkbenchBaseRow> selectBaseRows(PatientWorkbenchQuery query);

    @SelectProvider(type = PatientWorkbenchSqlProvider.class, method = "selectBuiltinGroupCounts")
    PatientWorkbenchBuiltinCounts selectBuiltinGroupCounts(PatientWorkbenchQuery query);

    @SelectProvider(type = PatientWorkbenchSqlProvider.class, method = "selectCustomGroupCounts")
    List<PatientWorkbenchGroupCount> selectCustomGroupCounts(PatientWorkbenchQuery query);

    @SelectProvider(type = PatientWorkbenchSqlProvider.class, method = "selectDoctorOptions")
    List<PatientWorkbenchDoctorOption> selectDoctorOptions(PatientWorkbenchQuery query);

    @SelectProvider(type = PatientWorkbenchSqlProvider.class, method = "selectSourceOptions")
    @ResultType(String.class)
    List<String> selectSourceOptions(PatientWorkbenchQuery query);

    @SelectProvider(type = PatientWorkbenchSqlProvider.class, method = "selectRelationOptions")
    @ResultType(String.class)
    List<String> selectRelationOptions(PatientWorkbenchQuery query);
}
