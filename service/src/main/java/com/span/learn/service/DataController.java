package com.span.learn.service;

import org.springframework.web.bind.annotation.*;

import javax.xml.crypto.Data;

@RestController
@RequestMapping("/data")
public class DataController {
    private final DataService service;

    public DataController(DataService service) {
        this.service = service;
    }

    @GetMapping
    public DataResponse getData(@RequestParam String id) {
        return service.getData(id);
    }

    @PostMapping
    public void updateData(@RequestParam String id,
                           @RequestParam String value) {
        service.updateData(id, value);
    }
}
