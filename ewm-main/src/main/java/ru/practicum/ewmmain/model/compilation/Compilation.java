package ru.practicum.ewmmain.model.compilation;

import lombok.*;
import ru.practicum.ewmmain.model.compilation.dto.CompilationNewDto;
import ru.practicum.ewmmain.model.event.Event;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * Объект Compilation
 * @author Evgeny S
 * @see ru.practicum.ewmmain.model.compilation.dto.CompilationDto
 * @see CompilationNewDto
 *
 */
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "compilations")
public class Compilation {
    /**
     * Id подборки.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "compilation_id")
    private Integer id;

    /**
     * Закрепление подборки на главной странице.
     */
    @Column(nullable = false)
    private Boolean pinned;

    /**
     * Заголовок.
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * События.
     */
    @OneToMany
    @JoinTable(name = "compilation_event_connector",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    private List<Event> events;

    /**
     * Equals по полю {@link #id}.
     * @return Equals по полю {@link #id}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Compilation that = (Compilation) o;

        return Objects.equals(id, that.id);
    }

    /**
     * HashCode по полю {@link #id}.
     * @return HashCode по полю {@link #id}.
     */
    @Override
    public int hashCode() {
        return id != null ? id : 0;
    }
}
