package com.rentevent.model.cliente;

import com.rentevent.model.Genero;
import com.rentevent.model.Role;
import com.rentevent.model.evento.Evento;
import com.rentevent.model.factura.Factura;
import com.rentevent.model.pregunta_segura.PreguntaSegura;
import com.rentevent.model.tarjeta.Tarjeta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cliente", uniqueConstraints = {@UniqueConstraint(columnNames = {"clie_usuario"})})
public class Cliente implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_cliente")
    @SequenceGenerator(name = "seq_cliente", sequenceName = "seq_cliente", allocationSize = 1)
    @Column(name = "clie_id")
    private Integer id;

    @Column(name = "clie_usuario", nullable = false) // se refiere al correo
    private String username;

    @Column(name = "clie_apellido")
    private String lastname;

    @Column(name = "clie_nombre")
    private String firstname;

    @Column(name = "clie_contrasenia")
    private String password;

    @Column(name = "clie_nacionalidad")
    private String nacionalidad;

    @Column(name = "clie_correo")
    private String correo;

    @Column(name = "clie_direccion")
    private String direccion;

    @Column(name = "clie_genero")
    private Genero genero;

    @Enumerated(EnumType.STRING)
    @Column(name = "clie_rol")
    private Role role;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Factura> facturas;
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tarjeta> tarjetas;
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Evento> eventos;
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PreguntaSegura> preguntaSeguras;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}