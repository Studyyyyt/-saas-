package com.example.springboot.service;

import com.example.springboot.entity.ConsentTemplate;
import com.example.springboot.mapper.ConsentTemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsentTemplateService {

    @Autowired
    private ConsentTemplateMapper consentTemplateMapper;

    public List<ConsentTemplate> selectAll() {
        return consentTemplateMapper.selectAll();
    }

    public List<ConsentTemplate> selectEnabled() {
        return consentTemplateMapper.selectEnabled();
    }

    public ConsentTemplate selectById(Long id) {
        return consentTemplateMapper.selectById(id);
    }

    public void add(ConsentTemplate item) {
        consentTemplateMapper.add(item);
    }

    public void edit(ConsentTemplate item) {
        consentTemplateMapper.edit(item);
    }

    public void delete(Long id) {
        consentTemplateMapper.delete(id);
    }
}
