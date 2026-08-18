package edu.ngd.config;

import edu.ngd.entity.FolderTemplate;
import edu.ngd.entity.Tag;
import edu.ngd.entity.User;
import edu.ngd.entity.UserRole;
import edu.ngd.repository.FolderTemplateRepository;
import edu.ngd.repository.TagRepository;
import edu.ngd.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FolderTemplateRepository folderTemplateRepository;
    private final TagRepository tagRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initAdminUser();
        initTestUser();
        initFolderTemplates();
        initDefaultTags();
    }

    private void initAdminUser() {
        Optional<User> adminOpt = userRepository.findByUsername("admin");
        if (adminOpt.isPresent()) {
            User admin = adminOpt.get();
            admin.setPassword(passwordEncoder.encode("admin123"));
            userRepository.save(admin);
            log.info("Admin password reset: admin/admin123");
        } else {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(UserRole.ADMIN)
                    .storageQuota(10737418240L)
                    .storageUsed(0L)
                    .build();
            userRepository.save(admin);
            log.info("Default admin user created: admin/admin123");
        }
    }

    private void initTestUser() {
        Optional<User> userOpt = userRepository.findByUsername("user");
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode("user123"));
            userRepository.save(user);
            log.info("User password reset: user/user123");
        } else {
            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .role(UserRole.USER)
                    .storageQuota(1073741824L)
                    .storageUsed(0L)
                    .build();
            userRepository.save(user);
            log.info("Default test user created: user/user123");
        }
    }

    private void initFolderTemplates() {
        List<FolderTemplate> existingTemplates = folderTemplateRepository.findAll();
        if (!existingTemplates.isEmpty()) {
            log.info("Folder templates already exist, skipping initialization");
            return;
        }

        FolderTemplate personal = FolderTemplate.builder()
                .name("个人文件夹")
                .parentId(null)
                .sortOrder(1)
                .build();
        personal = folderTemplateRepository.save(personal);

        folderTemplateRepository.save(FolderTemplate.builder().name("文档").parentId(personal.getId()).sortOrder(1).build());
        folderTemplateRepository.save(FolderTemplate.builder().name("图片").parentId(personal.getId()).sortOrder(2).build());
        folderTemplateRepository.save(FolderTemplate.builder().name("视频").parentId(personal.getId()).sortOrder(3).build());

        FolderTemplate work = FolderTemplate.builder()
                .name("工作文件夹")
                .parentId(null)
                .sortOrder(2)
                .build();
        work = folderTemplateRepository.save(work);

        folderTemplateRepository.save(FolderTemplate.builder().name("项目文档").parentId(work.getId()).sortOrder(1).build());
        folderTemplateRepository.save(FolderTemplate.builder().name("会议记录").parentId(work.getId()).sortOrder(2).build());
        folderTemplateRepository.save(FolderTemplate.builder().name("报告").parentId(work.getId()).sortOrder(3).build());

        FolderTemplate shared = FolderTemplate.builder()
                .name("共享文件夹")
                .parentId(null)
                .sortOrder(3)
                .build();
        shared = folderTemplateRepository.save(shared);

        folderTemplateRepository.save(FolderTemplate.builder().name("团队文档").parentId(shared.getId()).sortOrder(1).build());
        folderTemplateRepository.save(FolderTemplate.builder().name("知识库").parentId(shared.getId()).sortOrder(2).build());

        log.info("Default folder templates initialized");
    }

    private void initDefaultTags() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            createTagIfMissing(user.getId(), "重要", "#F56C6C");
            createTagIfMissing(user.getId(), "工作", "#409EFF");
            createTagIfMissing(user.getId(), "学习", "#67C23A");
            createTagIfMissing(user.getId(), "项目", "#E6A23C");
            createTagIfMissing(user.getId(), "待办", "#909399");
            createTagIfMissing(user.getId(), "归档", "#B37FEB");
        }
        log.info("Default tags initialized for all users");
    }

    private void createTagIfMissing(Long ownerId, String name, String color) {
        if (tagRepository.existsByNameAndOwnerId(name, ownerId)) {
            return;
        }
        Tag tag = Tag.builder()
                .name(name)
                .ownerId(ownerId)
                .color(color)
                .build();
        tagRepository.save(tag);
    }
}