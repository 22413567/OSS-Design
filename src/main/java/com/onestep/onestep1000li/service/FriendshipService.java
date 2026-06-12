package com.onestep.onestep1000li.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onestep.onestep1000li.entity.Friendship;
import com.onestep.onestep1000li.entity.User;
import com.onestep.onestep1000li.entity.Task;
import com.onestep.onestep1000li.entity.TaskShare;
import com.onestep.onestep1000li.repository.FriendshipRepository;
import com.onestep.onestep1000li.repository.UserRepository;
import com.onestep.onestep1000li.repository.TaskRepository;
import com.onestep.onestep1000li.repository.TaskShareRepository;

@Service
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskShareRepository taskShareRepository;

    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository,
                             TaskRepository taskRepository, TaskShareRepository taskShareRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.taskShareRepository = taskShareRepository;
    }

    @Transactional
    public String addFriend(String myId, String friendId) {
        if (myId.equals(friendId)) return "자기 자신은 친구로 추가할 수 없습니다.";

        User me = userRepository.findById(myId).orElseThrow();
        User friend = userRepository.findById(friendId).orElse(null);
        if (friend == null) return "존재하지 않는 사용자입니다.";

        if (friendshipRepository.existsByRequesterAndReceiver(me, friend)) {
            return "이미 등록된 친구입니다.";
        }

        int myFriendCount = friendshipRepository.countByRequester(me);
        int targetFriendCount = friendshipRepository.countByRequester(friend);
        if (myFriendCount >= 50 || targetFriendCount >= 50) {
            return "본인 혹은 친구의 친구 목록이 가득 찼습니다. 최대 50명까지만 추가할 수 있습니다.";
        }

        Friendship myFriendship = new Friendship();
        myFriendship.setRequester(me);
        myFriendship.setReceiver(friend);
        myFriendship.setStatus("ACCEPTED");
        friendshipRepository.save(myFriendship);

        Friendship friendFriendship = new Friendship();
        friendFriendship.setRequester(friend);
        friendFriendship.setReceiver(me);
        friendFriendship.setStatus("ACCEPTED");
        friendshipRepository.save(friendFriendship);

        return friend.getNickname() + "님과 성공적으로 친구가 되었습니다!";
    }

    public List<Friendship> getFriends(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        return friendshipRepository.findByRequester(user);
    }

    public boolean hasSharedTasks(String myId, String friendId) {
        List<TaskShare> allShares = taskShareRepository.findAll();
        for (TaskShare share : allShares) {
            String creatorId = share.getTask().getUser().getUserId();
            String receiverId = share.getUser().getUserId();
            if ((creatorId.equals(myId) && receiverId.equals(friendId)) ||
                (creatorId.equals(friendId) && receiverId.equals(myId))) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public String deleteFriend(String myId, String friendId) {
        User me = userRepository.findById(myId).orElseThrow();
        User friend = userRepository.findById(friendId).orElseThrow();

        if (!friendshipRepository.existsByRequesterAndReceiver(me, friend)) {
            throw new IllegalStateException("오류로 인해 친구 삭제에 실패함.");
        }

        List<TaskShare> allShares = taskShareRepository.findAll();
        for (TaskShare share : allShares) {
            String creatorId = share.getTask().getUser().getUserId();
            String receiverId = share.getUser().getUserId();
            
            if ((creatorId.equals(myId) && receiverId.equals(friendId)) ||
                (creatorId.equals(friendId) && receiverId.equals(myId))) {
                
                Task clone = share.getClonedTask();
                Task original = share.getTask();
                taskShareRepository.delete(share);
                
                if (clone != null) {
                    List<Task> cloneSubs = taskRepository.findByParentTask(clone);
                    taskRepository.deleteAll(cloneSubs);
                    taskRepository.delete(clone);
                }
                if (original != null) {
                    List<Task> originalSubs = taskRepository.findByParentTask(original);
                    taskRepository.deleteAll(originalSubs);
                    taskRepository.delete(original);
                }
            }
        }
        friendshipRepository.deleteByRequesterAndReceiver(me, friend);
        friendshipRepository.deleteByRequesterAndReceiver(friend, me);

        return friend.getNickname() + "님을 친구 목록에서 삭제했습니다.";
    }
}