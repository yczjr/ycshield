package com.blued.system.controller;

import com.blued.common.R;
import com.blued.system.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    @GetMapping("/all")
    public R<Map<String, Object>> all() {
        return R.ok(dictService.all());
    }

    @GetMapping("/{type}")
    public R<List<Map<String, Object>>> list(@PathVariable String type) {
        return R.ok(dictService.list(type));
    }

    @GetMapping("/attack-type/tree")
    public R<List<Map<String, Object>>> attackTypeTree() {
        return R.ok(dictService.attackTypeTree());
    }

    @PostMapping("/item")
    public R<Void> save(@RequestBody Map<String, Object> body) {
        dictService.saveItem(
                String.valueOf(body.get("dictType")),
                body.get("code") == null ? null : String.valueOf(body.get("code")),
                body.get("name") == null ? null : String.valueOf(body.get("name")),
                body.get("parentId") == null ? null : Long.valueOf(String.valueOf(body.get("parentId"))),
                body.get("sort") == null ? null : Integer.valueOf(String.valueOf(body.get("sort"))));
        return R.ok();
    }

    @DeleteMapping("/item/{id}")
    public R<Void> delete(@PathVariable Long id) {
        dictService.deleteItem(id);
        return R.ok();
    }
}
