package org.example.riskwarningsystembackend.service;

import org.example.riskwarningsystembackend.dto.CompanyGraphDTO;
import org.example.riskwarningsystembackend.entity.CompanyInfo;
import org.example.riskwarningsystembackend.entity.CompanyRelation;
import org.example.riskwarningsystembackend.repository.CompanyInfoRepository;
import org.example.riskwarningsystembackend.repository.CompanyRelationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyGraphService {

    private final CompanyInfoRepository companyInfoRepository;
    private final CompanyRelationRepository companyRelationRepository;

    public CompanyGraphService(CompanyInfoRepository companyInfoRepository, CompanyRelationRepository companyRelationRepository) {
        this.companyInfoRepository = companyInfoRepository;
        this.companyRelationRepository = companyRelationRepository;
    }

    public CompanyGraphDTO getCompanyGraph() {
        List<CompanyInfo> companies = companyInfoRepository.findAll();
        List<CompanyRelation> relations = companyRelationRepository.findAll();

        List<CompanyGraphDTO.Node> nodes = companies.stream()
                .map(company -> new CompanyGraphDTO.Node(
                        String.valueOf(company.getId()),
                        company.getName(),
                        20 // Default size
                ))
                .collect(Collectors.toList());

        List<CompanyGraphDTO.Edge> edges = relations.stream()
                .map(relation -> new CompanyGraphDTO.Edge(
                        String.valueOf(relation.getCompanyOneId()),
                        String.valueOf(relation.getCompanyTwoId()),
                        relation.getRelationType() + "(" + relation.getSharedProductName() + ")"
                ))
                .collect(Collectors.toList());

        return new CompanyGraphDTO(nodes, edges);
    }
}
