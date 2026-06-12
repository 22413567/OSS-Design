package com.onestep.onestep1000li.repository;

import com.onestep.onestep1000li.entity.Friendship;
import com.onestep.onestep1000li.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
    
    // 내가 추가한(요청한) 친구 목록 찾기
    List<Friendship> findByRequester(User requester);

    // 친구 중복 추가 방지
    boolean existsByRequesterAndReceiver(User requester, User receiver);

    // 친구 삭제 
    void deleteByRequesterAndReceiver(User requester, User receiver);

    // 친구 목록 최대 개수(50명) 제한을 위한 카운트 
    int countByRequester(User requester);
    
}