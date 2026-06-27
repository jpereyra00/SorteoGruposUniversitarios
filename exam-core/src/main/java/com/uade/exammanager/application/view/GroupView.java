package com.uade.exammanager.application.view;

import java.util.List;

public record GroupView(Long id,
                        String name,
                        List<GroupMemberView> members,
                        List<GroupTopicView> topics) {
}
