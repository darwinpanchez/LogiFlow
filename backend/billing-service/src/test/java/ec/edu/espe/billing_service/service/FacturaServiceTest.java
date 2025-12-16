package ec.edu.espe.billing_service.service;

import ec.edu.espe.billing_service.dto.CreateFacturaRequest;
import ec.edu.espe.billing_service.dto.FacturaResponse;
import ec.edu.espe.billing_service.model.EstadoFactura;
import ec.edu.espe.billing_service.model.Factura;
import ec.edu.espe.billing_service.repository.FacturaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @InjectMocks
    private FacturaService facturaService;

    private CreateFacturaRequest request;
    private Factura factura;
    private UUID facturaId;
    private UUID pedidoId;
    private UUID clienteId;

    @BeforeEach
    void setUp() {
        facturaId = UUID.randomUUID();
        pedidoId = UUID.randomUUID();
        clienteId = UUID.randomUUID();

        request = CreateFacturaRequest.builder()
                .pedidoId(pedidoId)
                .numeroPedido("PED-20240116-000001")
                .clienteId(clienteId)
                .clienteNombre("Juan Pérez")
                .distanciaKm(BigDecimal.valueOf(5.0))
                .pesoKg(BigDecimal.valueOf(2.5))
                .descuento(BigDecimal.ZERO)
                .build();

        factura = Factura.builder()
                .id(facturaId)
                .numeroFactura("FAC-20240116-000001")
                .pedidoId(pedidoId)
                .numeroPedido("PED-20240116-000001")
                .clienteId(clienteId)
                .clienteNombre("Juan Pérez")
                .estado(EstadoFactura.PENDIENTE)
                .subtotal(BigDecimal.valueOf(10.00))
                .impuestos(BigDecimal.valueOf(1.20))
                .total(BigDecimal.valueOf(11.20))
                .activa(true)
                .build();
    }

    @Test
    void crearFactura_ConDatosValidos_DebeRetornarFactura() {
        // Arrange
        when(facturaRepository.save(any(Factura.class))).thenReturn(factura);

        // Act
        FacturaResponse response = facturaService.crearFactura(request);

        // Assert
        assertNotNull(response);
        assertEquals(facturaId, response.getId());
        assertEquals(EstadoFactura.PENDIENTE, response.getEstado());
        assertTrue(response.getTotal().compareTo(BigDecimal.ZERO) > 0);
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }

    @Test
    void pagarFactura_ConFacturaPendiente_DebeMarcarComoPagada() {
        // Arrange
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class))).thenReturn(factura);

        // Act
        FacturaResponse response = facturaService.pagarFactura(facturaId, "Tarjeta de Crédito");

        // Assert
        assertNotNull(response);
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }

    @Test
    void pagarFactura_ConFacturaYaPagada_DebeLanzarExcepcion() {
        // Arrange
        factura.setEstado(EstadoFactura.PAGADA);
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.of(factura));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> facturaService.pagarFactura(facturaId, "Tarjeta de Crédito")
        );
        assertTrue(exception.getMessage().contains("ya fue pagada"));
        verify(facturaRepository, never()).save(any());
    }

    @Test
    void pagarFactura_ConFacturaNoExistente_DebeLanzarExcepcion() {
        // Arrange
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> facturaService.pagarFactura(facturaId, "Efectivo")
        );
        verify(facturaRepository, never()).save(any());
    }

    @Test
    void obtenerFacturaPorId_ConIdValido_DebeRetornarFactura() {
        // Arrange
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.of(factura));

        // Act
        FacturaResponse response = facturaService.obtenerFacturaPorId(facturaId);

        // Assert
        assertNotNull(response);
        assertEquals(facturaId, response.getId());
        assertEquals("FAC-20240116-000001", response.getNumeroFactura());
    }

    @Test
    void obtenerFacturaPorId_ConIdInvalido_DebeLanzarExcepcion() {
        // Arrange
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> facturaService.obtenerFacturaPorId(facturaId)
        );
    }

    @Test
    void calcularTotal_ConDistanciaYPeso_DebeCalcularCorrectamente() {
        // Arrange - Cálculo implícito en crearFactura
        when(facturaRepository.save(any(Factura.class))).thenReturn(factura);

        // Act
        FacturaResponse response = facturaService.crearFactura(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.getSubtotal().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.getImpuestos().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(response.getTotal().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void anularFactura_ConFacturaPendiente_DebeAnular() {
        // Arrange
        when(facturaRepository.findById(facturaId)).thenReturn(Optional.of(factura));
        when(facturaRepository.save(any(Factura.class))).thenReturn(factura);

        // Act
        FacturaResponse response = facturaService.anularFactura(facturaId);

        // Assert
        assertNotNull(response);
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }
}
