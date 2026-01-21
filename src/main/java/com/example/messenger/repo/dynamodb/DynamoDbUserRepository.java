package com.example.messenger.repo.dynamodb;

import com.example.messenger.domain.User;
import com.example.messenger.repo.api.GenericUserRepository;
import com.example.messenger.repo.dynamodb.mapper.UserMapper;
import com.example.messenger.repo.dynamodb.model.UserDynamoModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Profile("dynamodb")
public class DynamoDbUserRepository implements GenericUserRepository {
    private final DynamoDbTable<UserDynamoModel> userTable;
    private final UserMapper mapper;

    public DynamoDbUserRepository(DynamoDbEnhancedClient enhancedClient, UserMapper mapper) {
        this.userTable = enhancedClient.table("Users", TableSchema.fromBean(UserDynamoModel.class));
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserDynamoModel model = mapper.toModel(user);
        userTable.putItem(model);
        return mapper.toDomain(model);
    }

    @Override
    public Optional<User> findById(UUID id) {
        UserDynamoModel model = userTable.getItem(r -> r.key(k -> k.partitionValue(id.toString())));
        return Optional.ofNullable(model).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        DynamoDbIndex<UserDynamoModel> usernameIndex = userTable.index("username-index");
        List<UserDynamoModel> results = usernameIndex.query(r -> r.queryConditional(
                        QueryConditional.keyEqualTo(k -> k.partitionValue(username))
                )).stream()
                .flatMap(page -> page.items().stream())
                .toList();

        return results.isEmpty() ? Optional.empty() : Optional.of(mapper.toDomain(results.getFirst()));
    }

    @Override
    public List<User> findAll() {
        return userTable.scan().items().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        userTable.deleteItem(r -> r.key(k -> k.partitionValue(id.toString())));
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }
}