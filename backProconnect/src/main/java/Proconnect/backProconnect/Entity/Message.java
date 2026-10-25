package Proconnect.backProconnect.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "Messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Sender ID cannot be null") @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /** Relación de solo lectura (LAZY) sobre la FK sender_id; se escribe con el campo Long. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "sender_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_messages_sender"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User sender;

    @NotNull(message = "Receiver ID cannot be null") @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    /** Relación de solo lectura (LAZY) sobre la FK receiver_id; se escribe con el campo Long. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "receiver_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_messages_receiver"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User receiver;

    @NotBlank(message = "Content is mandatory") @Column(nullable = false)
    private String content;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    /** Requerido por JPA para materializar la entidad. */
    public Message() {}

    public Message(Long id, Long senderId, Long receiverId, String content, Instant sentAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.sentAt = sentAt != null ? sentAt : Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public User getSender() {
        return sender;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
}
