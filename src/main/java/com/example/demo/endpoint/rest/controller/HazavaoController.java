package com.example.demo.endpoint.rest.controller;


import com.example.demo.service.HazavaoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
public class HazavaoController {

    private final HazavaoService hazavaoService;

    @GetMapping("/hazavao")
    public ResponseEntity<Map<String, String>> getDefinition(@RequestParam String teny) {
        try {
            String definition = hazavaoService.getDefinition(teny);
            return ResponseEntity.ok(Map.of(
                    "teny", teny,
                    "definition", definition
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Erreur lors de la récupération de la définition",
                            "message", e.getMessage()
                    ));
        }
    }
}