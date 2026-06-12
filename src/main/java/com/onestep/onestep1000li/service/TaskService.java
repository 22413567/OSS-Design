package com.onestep.onestep1000li.service;

import com.onestep.onestep1000li.entity.Task;
import com.onestep.onestep1000li.entity.TaskShare;
import com.onestep.onestep1000li.entity.User;
import com.onestep.onestep1000li.repository.TaskRepository;
import com.onestep.onestep1000li.repository.TaskShareRepository;
import com.onestep.onestep1000li.repository.UserRepository;
import com.onestep.onestep1000li.repository.FriendshipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskShareRepository taskShareRepository;
    private final FriendshipRepository friendshipRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, 
                       TaskShareRepository taskShareRepository, FriendshipRepository friendshipRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskShareRepository = taskShareRepository;
        this.friendshipRepository = friendshipRepository;
    }
    
    @Transactional
    public Task addSharedTask(String userId, String content, String friendId) {
        
        if (content == null || content.trim().isEmpty() || content.length() > 50) {
            throw new IllegalArgumentException("과제 내용은 1자 이상, 50자 이내로 입력해주세요.");
        }

        User creator = userRepository.findById(userId).orElseThrow();

        // 메인 과제 중복 검사
        if (taskRepository.existsByUserAndContentAndParentTaskIsNullAndIsCompletedFalse(creator, content)) {
            throw new IllegalArgumentException("이미 동일한 이름의 과제가 진행 중입니다. 다른 이름을 입력해 주세요.");
        }

        // 친구가 있는 경우 추가 검증
        if (friendId != null && !friendId.equals("none")) {
            User friend = userRepository.findById(friendId).orElseThrow();

            // 11a. 상대방이 나를 지웠거나, 내가 지웠는데 화면에 남은 경우
            if (!friendshipRepository.existsByRequesterAndReceiver(creator, friend)) {
                throw new IllegalArgumentException("친구 목록에 존재하지 않는 사용자입니다. 친구 상태를 확인해주세요.");
            }

            // 44a. 공동 과제 중복 검사
            // 상대방의 메인 과제 중 "[공동] + content" 이름을 가진 완료되지 않은 과제가 있는지 검사
            String sharedTaskName = "[공동] " + content;
            if (taskRepository.existsByUserAndContentAndParentTaskIsNullAndIsCompletedFalse(friend, sharedTaskName)) {
                throw new IllegalArgumentException(friend.getNickname() + "님과 이미 동일한 이름의 과제를 진행 중입니다.");
            }
        }

        // 과제 저장
        Task myTask = new Task();
        myTask.setUser(creator);
        myTask.setContent(content);
        myTask.setIsCompleted(false);
        myTask.setIsMain(true);
        myTask.setOriginalTaskId(null);
        taskRepository.save(myTask);

        if (friendId != null && !friendId.equals("none")) {
            User friend = userRepository.findById(friendId).orElseThrow();
            Task friendTask = new Task();
            friendTask.setUser(friend);
            friendTask.setContent("[공동] " + content);
            friendTask.setIsCompleted(false);
            friendTask.setIsMain(true);
            friendTask.setOriginalTaskId(myTask.getTaskId()); 
            taskRepository.save(friendTask);
            
            TaskShare share = new TaskShare();
            share.setTask(myTask);
            share.setClonedTask(friendTask);
            share.setUser(friend);
            taskShareRepository.save(share);
        }
        return myTask;
    }
    
 // 하위 과제 추가
    @Transactional
    public Task addSubTask(String userId, String content, Integer parentTaskId) {
        
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("하위 과제 내용을 입력해주세요.");
        }

        User user = userRepository.findById(userId).orElseThrow();
        Task parentTask = taskRepository.findById(parentTaskId).orElseThrow();

        // 공동 과제의 경우, 원본 참조 ID가 있다면(= 내가 리더가 아니라면) 하위 과제 추가 차단
        if (parentTask.getOriginalTaskId() != null) {
            throw new IllegalArgumentException("공동 과제의 하위 과제는 리더만 추가할 수 있습니다.");
        }

        // 개수 및 중복 방어
        int currentSubTaskCount = taskRepository.countByParentTask(parentTask);
        if (currentSubTaskCount >= 10) {
            throw new IllegalArgumentException("하나의 메인 과제에는 최대 10개의 하위 과제만 등록할 수 있습니다.");
        }
        boolean isDuplicate = taskRepository.existsByParentTaskAndContent(parentTask, content);
        if (isDuplicate) {
            throw new IllegalArgumentException("해당 메인 과제에 이미 동일한 이름의 하위 과제가 존재합니다.");
        }

        // 리더의 하위 과제 저장
        Task subTask = new Task();
        subTask.setUser(user);
        subTask.setContent(content.trim());
        subTask.setIsCompleted(false);
        subTask.setIsMain(false);
        subTask.setParentTask(parentTask);
        taskRepository.save(subTask);

        // 친구의 메인 과제 밑에도 동일하게 달아주기
        List<TaskShare> shares = taskShareRepository.findByTask(parentTask);
        for (TaskShare share : shares) {
            Task clonedSub = new Task();
            clonedSub.setUser(share.getUser());
            clonedSub.setContent(content.trim());
            clonedSub.setIsCompleted(false);
            clonedSub.setIsMain(false);
            clonedSub.setParentTask(share.getClonedTask());
            clonedSub.setOriginalTaskId(subTask.getTaskId()); 
            taskRepository.save(clonedSub);
        }

        return subTask;
    }
 // 과제 수정 (글자 수, 완료 여부, 권한 체크)
    @Transactional
    public Task updateTask(Integer taskId, String newContent, String currentUserId) {
        // 44c. 50자 초과 및 공백 검사
        if (newContent == null || newContent.trim().isEmpty() || newContent.length() > 50) {
            throw new IllegalArgumentException("과제 내용은 최대 50자까지 입력 가능합니다.");
        }

        Task task = taskRepository.findById(taskId).orElseThrow();

        // 11a. 완료된 과제 수정 방지
        if (task.getIsCompleted()) {
            throw new IllegalArgumentException("완료된 과제는 수정할 수 없습니다.");
        }

        // 44a. 권한 체크 1 (내 과제가 아닌 경우)
        if (!task.getUser().getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("해당 과제를 수정할 권한이 없습니다.");
        }

        // 44a. 권한 체크 2 (공동 과제인데 내가 리더가 아닌 복제본인 경우)
        if (task.getOriginalTaskId() != null) {
            throw new IllegalArgumentException("해당 과제를 수정할 권한이 없습니다."); 
        }

        task.setContent(newContent.trim());
        
        List<Task> clones = taskRepository.findByOriginalTaskId(taskId);
        for (Task clone : clones) {
            // 메인 과제일 때만 [공동]을 붙이고, 하위 과제면 그대로 복사
            if (task.getIsMain()) {
                clone.setContent("[공동] " + newContent.trim());
            } else {
                clone.setContent(newContent.trim());
            }
        }
        
        return taskRepository.save(task);
    }

    // =========================================================================
    // 메인 - 하위 과제 연쇄 삭제
    // =========================================================================
    @Transactional
    public void deleteTask(Integer taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow();

        // 1. 리더 권한 체크
        if (task.getOriginalTaskId() != null) {
            throw new RuntimeException("공동 과제는 리더만 삭제할 수 있습니다.");
        }

        // 2. 공유 기록(TaskShare) 및 친구의 복제본들 먼저 삭제
        List<TaskShare> shares = taskShareRepository.findByTask(task);
        for (TaskShare share : shares) {
            Task clone = share.getClonedTask();
            if (clone != null) {
                // 친구의 하위 과제들 삭제
                List<Task> cloneSubs = taskRepository.findByParentTask(clone);
                taskRepository.deleteAll(cloneSubs);
                // 친구의 메인 과제 삭제
                taskRepository.delete(clone);
            }
        }
        // 기록 삭제
        taskShareRepository.deleteAll(shares);

        // 3. 만약 하위 과제 공유가 있었다면 그 복제본들도 삭제
        List<Task> subClones = taskRepository.findByOriginalTaskId(taskId);
        taskRepository.deleteAll(subClones);

        // 4. 나의 하위 과제들 삭제
        List<Task> mySubs = taskRepository.findByParentTask(task);
        taskRepository.deleteAll(mySubs);

        // 5. 본인(메인 과제) 삭제
        taskRepository.delete(task);
    }

    public List<Task> getTasksByUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return taskRepository.findByUser(user);
    }

    @Transactional
    public Task toggleTask(Integer taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow();
        task.setIsCompleted(!task.getIsCompleted());
        taskRepository.save(task);

        if (task.getParentTask() != null) {
            Task parent = task.getParentTask();
            List<Task> allTasks = taskRepository.findByParentTask(parent);
            boolean isAllCompleted = true;
            for (Task t : allTasks) {
                if (!t.getIsCompleted()) { isAllCompleted = false; break; }
            }
            parent.setIsCompleted(isAllCompleted);
            taskRepository.save(parent);
        }
        return task;
    }
}