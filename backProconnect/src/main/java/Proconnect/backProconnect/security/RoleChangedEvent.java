package Proconnect.backProconnect.security;

/** Publicado por RoleService al cambiar un rol; RoleResolver invalida su cache tras el commit. */
public record RoleChangedEvent(Long userId) {}
