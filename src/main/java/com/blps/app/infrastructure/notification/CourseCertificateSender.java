package com.blps.app.infrastructure.notification;

import com.blps.app.domain.model.Course;
import com.blps.app.domain.model.AppUser;

public interface CourseCertificateSender {

    boolean sendCourseCompletionCertificate(AppUser user, Course course);
}
