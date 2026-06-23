package com.janginharou.domain.ai.controller;

import com.janginharou.domain.ai.dto.ExplainRequest;
import com.janginharou.domain.ai.dto.ExplainResponse;
import com.janginharou.domain.ai.service.ExplainService;
import com.janginharou.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Explain API", description = "전통문화 AI 해설 API")
public class ExplainController {

    private final ExplainService explainService;

    @PostMapping("/explain")
    @Operation(
            summary = "전통문화 AI 해설",
            description = "질문에 대한 해설과 관련 체험을 반환합니다. "
                    + "fallback은 Spring 합성 fallback 사용 여부이며 현재는 항상 false입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "해설 성공",
                    content = @Content(
                            schema = @Schema(implementation = ExplainResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "message": "Success",
                                      "data": {
                                        "answer": "매듭장은 전통 매듭 공예를 전승하는 장인입니다.",
                                        "sources": [],
                                        "matchingKeywords": ["매듭장"],
                                        "recommendedCategories": ["공예"],
                                        "recommendedTags": ["매듭장", "공예"],
                                        "relatedExperiences": [],
                                        "fallback": false,
                                        "message": null
                                      }
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청값 검증 실패"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "503",
                    description = "AI 서비스 사용 불가"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "504",
                    description = "AI 서비스 응답 시간 초과"
            )
    })
    public ResponseEntity<ApiResponse<ExplainResponse>> explain(
            @Valid @RequestBody ExplainRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(explainService.explain(request)));
    }
}
