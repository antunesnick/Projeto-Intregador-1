package edu.unoeste.temperatureAPI.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// ... imports
@Entity
@Table(name = "sensor_logs")
@Data
public class SensorLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "temp_1")
    private Double temp1;

    @Column(name = "temp_2")
    private Double temp2;

    @Column(name = "temp_3")
    private Double temp3;

    private Double current;
    private Integer doorOpenCount;
    private Boolean isDoorCurrentlyOpen;

    @CreationTimestamp
    private LocalDateTime timestamp;
}