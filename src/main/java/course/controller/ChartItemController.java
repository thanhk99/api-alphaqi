package course.controller;

import course.dto.ChartItemRequest;
import course.dto.ChartItemResponse;
import course.service.ChartItemService;
import course.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chart-items")
@RequiredArgsConstructor
public class ChartItemController {
    private final ChartItemService chartItemService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChartItemResponse>>> getAllChartItems() {
        List<ChartItemResponse> items = chartItemService.getAllChartItems();
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ChartItemResponse>> createChartItem(
            @RequestBody @Valid ChartItemRequest request) {
        ChartItemResponse response = chartItemService.createChartItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Chart item created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ChartItemResponse>> updateChartItem(
            @PathVariable Long id,
            @RequestBody @Valid ChartItemRequest request) {
        ChartItemResponse response = chartItemService.updateChartItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Chart item updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteChartItem(@PathVariable Long id) {
        chartItemService.deleteChartItem(id);
        return ResponseEntity.ok(ApiResponse.success("Chart item deleted successfully", null));
    }
}
