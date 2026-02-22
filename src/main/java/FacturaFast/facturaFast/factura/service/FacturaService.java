package FacturaFast.facturaFast.factura.service;


import FacturaFast.facturaFast.detalle.entity.DetallePreciso;
import FacturaFast.facturaFast.detalle.repository.DetallePrecisoRepository;
import FacturaFast.facturaFast.empresa.entity.Empresa;
import FacturaFast.facturaFast.empresa.service.EmpresaService;
import FacturaFast.facturaFast.factura.entity.EstadoFactura;
import FacturaFast.facturaFast.factura.entity.Factura;
import FacturaFast.facturaFast.factura.entity.Tipo;
import FacturaFast.facturaFast.factura.repository.FacturaRepository;
import FacturaFast.facturaFast.usuario.entity.EstadoBot;
import FacturaFast.facturaFast.usuario.entity.UsuarioBot;
import FacturaFast.facturaFast.usuario.repository.UsuarioBotRepository;
import FacturaFast.facturaFast.usuario.service.SunatInfoService;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepo;
    private final UsuarioBotRepository usuarioRepo;
    private final DetallePrecisoRepository detalleRepo;
    private final EmpresaService empresaService;
    private final SunatInfoService sunatInfoService;

    private static final BigDecimal IGV_DIVISOR = new BigDecimal("1.18");
    private static final BigDecimal IGV_TASA = new BigDecimal("0.18");

    public FacturaService(FacturaRepository facturaRepo, UsuarioBotRepository usuarioRepo,
                          DetallePrecisoRepository detalleRepo, EmpresaService empresaService,
                          SunatInfoService sunatInfoService){

        this.facturaRepo = facturaRepo;
        this.usuarioRepo = usuarioRepo;
        this.detalleRepo = detalleRepo;
        this.empresaService = empresaService;
        this.sunatInfoService = sunatInfoService;
    }

    @Transactional // /emitir ruc
    public Factura iniciarBorrador(Long telegramId, String rucCliente){
        UsuarioBot usuario = usuarioRepo.findById(telegramId).orElseThrow(() -> new RuntimeException("Usuario no encontrado," +
                "inicia con /start"));
        Empresa empresa = empresaService.obtenerEmpresaUsuario(telegramId);
        SunatInfoService.InfoEmpresa infoCliente;
        try {
            infoCliente = sunatInfoService.consultarRuc(rucCliente);
        }catch (Exception e){
            infoCliente = SunatInfoService.InfoEmpresa.builder()
                    .razonSocial("SIN NOMBRE")
                    .direccion("-")
                    .build();
        }

        Integer ultimoCorrelativo = facturaRepo.findMaxCorrelativoByEmpresaId(empresa.getId());
        Integer nuevoCorrelativo = (ultimoCorrelativo == null) ? 1 : ultimoCorrelativo + 1;

        Factura factura = Factura.builder()
                .empresa(empresa)
                .usuario(usuario)
                .serie("F001")
                .correlativo(nuevoCorrelativo)
                .tipo(Tipo.FACTURA)
                .clienteRuc(rucCliente)
                .clienteNombre(infoCliente.getRazonSocial()) //nombre de la empresa (cliente)
                .direccionCliente(infoCliente.getDireccion())
                .issueDate(LocalDateTime.now())
                .currency("PEN")
                .estadoFactura(EstadoFactura.DRAFT)
                .totalGravada(BigDecimal.ZERO)
                .totalIgv(BigDecimal.ZERO)
                .totalCantidad(BigDecimal.ZERO)
                .build();
        factura = facturaRepo.save(factura);

        usuario.setFacturaActual(factura);
        usuario.setEstadoActual(EstadoBot.ADDING_ITEMS);
        usuarioRepo.save(usuario);


        return factura;
    }

    @Transactional
    public Factura agregarDetalle(Long telegramId, BigDecimal cantidad, String descripcion,
                                  BigDecimal precioConIgv){

        UsuarioBot usuario = usuarioRepo.findById(telegramId).orElseThrow();
        Factura factura = usuario.getFacturaActual();

        if(factura == null) throw new RuntimeException("No hay factura activa");

        //precio 1.18
        BigDecimal valorUnitario = precioConIgv.divide(IGV_DIVISOR, 2, RoundingMode.HALF_UP);

        //cantidad * precioconiigv
        BigDecimal costoTotal = precioConIgv.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);

        // total sin igv
        BigDecimal valorVentaTotal = valorUnitario.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);

        // igv total : total - total sin igv
        BigDecimal igvLinea = costoTotal.subtract(valorVentaTotal);

        DetallePreciso detalle = DetallePreciso.builder()
                .factura(factura)
                .cantidad(cantidad)
                .descripcion(descripcion)
                .precioUnitarioConIgv(precioConIgv)
                .valorUnitario(valorUnitario)
                .igv(igvLinea)
                .costoTotal(costoTotal)
                .unitCode("NIU")
                .tipoIgv("10") //meeeh
                .build();

        detalleRepo.save(detalle);
        actualizarTotalesFactura(factura);
        return factura;
    }

    private void actualizarTotalesFactura(Factura factura){

        List<DetallePreciso> items = detalleRepo.findByFactura_Id(factura.getId());

        BigDecimal totalFinal = items.stream()
                .map(DetallePreciso::getCostoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIgv = items.stream()
                .map(DetallePreciso::getIgv)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        //total - igv
        BigDecimal totalBase = totalFinal.subtract(totalIgv);

        factura.setTotalCantidad(totalFinal);
        factura.setTotalIgv(totalIgv);
        factura.setTotalGravada(totalBase);

        facturaRepo.save(factura);
    }

}
