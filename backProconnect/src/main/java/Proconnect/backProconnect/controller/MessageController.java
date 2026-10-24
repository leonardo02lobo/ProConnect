package Proconnect.backProconnect.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.model.MessageDto;
import Proconnect.backProconnect.model.MessageRequest;
import Proconnect.backProconnect.security.CurrentUser;
import Proconnect.backProconnect.service.MessageService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<MessageDto> send(@RequestBody @Valid MessageRequest request) {
        MessageDto created = messageService.send(CurrentUser.id(), request);
        return ResponseEntity.created(URI.create("/api/messages/" + created.id())).body(created);
    }

    /** Mensajes enviados y recibidos por el usuario de la sesión. */
    @GetMapping
    public List<MessageDto> mine() {
        return messageService.listFor(CurrentUser.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        messageService.delete(id);
    }
}
