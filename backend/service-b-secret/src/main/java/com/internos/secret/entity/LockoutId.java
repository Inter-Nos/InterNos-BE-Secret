package com.internos.secret.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockoutId implements Serializable {
    private Long roomId;
    private String ipHash;
}

