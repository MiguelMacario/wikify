package com.wikify.entity;

import com.wikify.entity.enums.Theme;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settings")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Settings {

    @Id
    private Long id;

    @Column(name = "app_name", nullable = false, length = 60)
    private String appName;

    @Column(name = "logo_media_id")
    private UUID logoMediaId;

    @Column(name = "theme", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Theme theme;

    @Column(name = "primary_color", length = 7)
    private String primaryColor;

    @Column(name = "home_title", length = 120)
    private String homeTitle;

    @Column(name = "home_content", columnDefinition = "TEXT")
    private String homeContent;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
}
