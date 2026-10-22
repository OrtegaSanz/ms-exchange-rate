package com.nttdata.ms.exchange.rate.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "consultas_cambio")
public class ConsultaCambio {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_acceso")
    private Date fechaAcceso;

    @Enumerated(EnumType.STRING)
    @Column(name = "direccion", nullable = false, length = 20)
    private DireccionCambio direccion;

    @Column(name = "monto_soles", nullable = false, precision = 19, scale = 2)
    private BigDecimal montoSoles;

    @Column(name = "monto_dolares", nullable = false, precision = 19, scale = 2)
    private BigDecimal montoDolares;

    @Column(name = "tipo_cambio_aplicado", nullable = false, precision = 19, scale = 6)
    private BigDecimal tipoCambioAplicado;

    public ConsultaCambio(Cliente cliente,
                          Date fechaAcceso,
                          DireccionCambio direccion,
                          BigDecimal montoSoles,
                          BigDecimal montoDolares,
                          BigDecimal tipoCambioAplicado) {
        this.cliente = cliente;
        this.fechaAcceso = fechaAcceso;
        this.direccion = direccion;
        this.montoSoles = montoSoles;
        this.montoDolares = montoDolares;
        this.tipoCambioAplicado = tipoCambioAplicado;
    }

    public BigDecimal getMontoOrigen() {
        return direccion == DireccionCambio.SOLES_A_DOLARES ? montoSoles : montoDolares;
    }

    public BigDecimal getMontoDestino() {
        return direccion == DireccionCambio.SOLES_A_DOLARES ? montoDolares : montoSoles;
    }
}
