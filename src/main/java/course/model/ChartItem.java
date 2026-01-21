package course.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String label; // Key (e.g., "Cổ phiếu", "Trái phiếu")

    @Column(nullable = false)
    private Double value; // Value (e.g., 50.0, 30.0)

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
