package study.counsel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.counsel.entity.ChatSequenceNumber;
import study.counsel.repository.ChatSequenceNumberRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class SequenceService {

    private final ChatSequenceNumberRepository chatSequenceNumberRepository;

    public ChatSequenceNumber increaseSeq() {
        ChatSequenceNumber chatSequenceNumber = ChatSequenceNumber.createNew();
        return chatSequenceNumberRepository.save(chatSequenceNumber);
    }
}
