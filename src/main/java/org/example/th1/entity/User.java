package org.example.th1.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdUser")
    private Integer idUser;

    @Column(name = "UserName", length = 255)
    private String userName;

    @Column(name = "Password", length = 255)
    private String password;

    @Column(name = "Token", length = 255)
    private String token;
}
