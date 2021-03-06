package com.sequenceiq.mock.clouderamanager.v31.controller;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.sequenceiq.mock.swagger.model.ApiEcho;
import com.sequenceiq.mock.swagger.v31.api.ToolsResourceApi;

@Controller
public class ToolsResourceV31Controller implements ToolsResourceApi {

    @Override
    public ResponseEntity<ApiEcho> echo(String mockUuid, @Valid String message) {
        message = message == null ? "Hello World!" : message;
        return ResponseEntity.ok(new ApiEcho().message(message));
    }

}
