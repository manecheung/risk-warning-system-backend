package org.example.riskwarningsystembackend.controller;

import org.example.riskwarningsystembackend.common.RestResult;
import org.example.riskwarningsystembackend.dto.CompanyGraphDTO;
import org.example.riskwarningsystembackend.service.CompanyGraphService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company-graph")
public class CompanyGraphController {

    private final CompanyGraphService companyGraphService;

    public CompanyGraphController(CompanyGraphService companyGraphService) {
        this.companyGraphService = companyGraphService;
    }

    @GetMapping
    public RestResult<CompanyGraphDTO> getCompanyGraph() {
        return RestResult.success(companyGraphService.getCompanyGraph());
    }
}
