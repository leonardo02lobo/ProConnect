package Proconnect.backProconnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Proconnect.backProconnect.model.MessageDto;
import Proconnect.backProconnect.service.MessageService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/message")
@CrossOrigin(origins = "*")
public class MessageController {
    
    private final MessageService messageService;

    public MessageController(MessageService messageService){
        this.messageService = messageService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> AddMessage(@RequestBody @Valid MessageDto messageDto){
        return ResponseEntity.ok().body(messageService.AddMessage(messageDto));
    }

    @GetMapping("list/{userId}")
    public ResponseEntity<?> ListMessageById(@PathVariable Long senderId){
        return ResponseEntity.ok().body(messageService.ListMessagesByUserId(senderId));
    }

    @DeleteMapping("/delete/{Id}")
    public ResponseEntity<?> DeleteMapping(@PathVariable Long Id){
        boolean isDelete = messageService.DeleteMessageById(Id);
        if(isDelete){
            return ResponseEntity.ok().body("Message Id Successufly");
        }else{
            return ResponseEntity.badRequest().body("Does not delete Message");
        }
    }
}
