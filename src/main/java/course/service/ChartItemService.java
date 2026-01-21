package course.service;

import course.dto.ChartItemRequest;
import course.dto.ChartItemResponse;
import course.exception.BadRequestException;
import course.exception.ResourceNotFoundException;
import course.model.ChartItem;
import course.repository.ChartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartItemService {
    private final ChartItemRepository chartItemRepository;

    @Transactional(readOnly = true)
    public List<ChartItemResponse> getAllChartItems() {
        return chartItemRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChartItemResponse createChartItem(ChartItemRequest request) {
        if (chartItemRepository.findByLabel(request.getLabel()).isPresent()) {
            throw new BadRequestException("Label '" + request.getLabel() + "' already exists");
        }

        ChartItem item = ChartItem.builder()
                .label(request.getLabel())
                .value(request.getValue())
                .build();

        return convertToResponse(chartItemRepository.save(item));
    }

    @Transactional
    public ChartItemResponse updateChartItem(Long id, ChartItemRequest request) {
        ChartItem item = chartItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chart item not found with id: " + id));

        // Check if new label already exists in another item
        chartItemRepository.findByLabel(request.getLabel())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException("Label '" + request.getLabel() + "' already exists");
                    }
                });

        item.setLabel(request.getLabel());
        item.setValue(request.getValue());

        return convertToResponse(chartItemRepository.save(item));
    }

    @Transactional
    public void deleteChartItem(Long id) {
        if (!chartItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Chart item not found with id: " + id);
        }
        chartItemRepository.deleteById(id);
    }

    private ChartItemResponse convertToResponse(ChartItem item) {
        return ChartItemResponse.builder()
                .id(item.getId())
                .label(item.getLabel())
                .value(item.getValue())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
