package kr.ac.knue.commonfoundation.foundation;

import java.util.Map;

import kr.ac.knue.commonfoundation.auth.CurrentUserContext;
import kr.ac.knue.commonfoundation.datascope.DataScopeMapper;
import kr.ac.knue.commonfoundation.datascope.DataScopeSearchCondition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FoundationAccessService {

    private final CurrentUserContext currentUserContext;
    private final DataScopeMapper dataScopeMapper;

    public FoundationAccessService(CurrentUserContext currentUserContext, DataScopeMapper dataScopeMapper) {
        this.currentUserContext = currentUserContext;
        this.dataScopeMapper = dataScopeMapper;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> currentDataScope() {
        var user = currentUserContext.current().orElseThrow().user();
        long permissionRows = dataScopeMapper.countDataScopes(DataScopeSearchCondition.of(1, 20, null, null, null));
        return Map.of(
            "userId", user.userId(),
            "roles", user.roles(),
            "dataScope", user.dataScope(),
            "enforced", true,
            "permissionRows", permissionRows
        );
    }
}
