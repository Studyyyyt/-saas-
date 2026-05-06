package com.example.springboot.service;

import com.example.springboot.entity.TreatmentCatalog;
import com.example.springboot.mapper.TreatmentCatalogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TreatmentCatalogService {

    @Autowired
    private TreatmentCatalogMapper treatmentCatalogMapper;

    public List<TreatmentCatalog> selectAll() {
        return treatmentCatalogMapper.selectAll();
    }

    public List<TreatmentCatalog> selectEnabled() {
        return treatmentCatalogMapper.selectEnabled();
    }

    public TreatmentCatalog selectById(Long id) {
        return treatmentCatalogMapper.selectById(id);
    }

    public void add(TreatmentCatalog item) {
        treatmentCatalogMapper.add(item);
    }

    public void edit(TreatmentCatalog item) {
        treatmentCatalogMapper.edit(item);
    }

    public void delete(Long id) {
        treatmentCatalogMapper.delete(id);
    }
}
