package net.cosmocat.marketplace.controller;

import lombok.RequiredArgsConstructor;
import net.cosmocat.marketplace.database.dal.service.CosmoCatService;
import net.cosmocat.marketplace.database.dto.entity.CosmoCatDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cosmo-cats")
@RequiredArgsConstructor
public class CosmoCatController {

    private final CosmoCatService cosmoCatService;

    @GetMapping
    public ResponseEntity<List<CosmoCatDTO>> getCosmoCats() {
        List<CosmoCatDTO> cats = cosmoCatService.getCosmoCats();
        return ResponseEntity.ok(cats);
    }

}
