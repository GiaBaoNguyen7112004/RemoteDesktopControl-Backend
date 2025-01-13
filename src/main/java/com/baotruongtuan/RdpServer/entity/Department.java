package com.baotruongtuan.RdpServer.entity;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String name;

    @JoinColumn(name = "code")
    String code;

    @JsonIgnore
    @OneToMany(mappedBy = "department", fetch = FetchType.EAGER)
    List<DepartmentDetail> departmentDetails;

    public void setCode() {
        this.code = UUID.randomUUID().toString().substring(0, 10);
    }

    public void removeUser(int userId) {
        this.getDepartmentDetails()
                .removeAll(this.getDepartmentDetails().stream()
                        .filter(departmentDetail ->
                                departmentDetail.getDepartment().getId() == this.getId()
                                        && departmentDetail.getUser().getId() == userId)
                        .toList());
    }
}
