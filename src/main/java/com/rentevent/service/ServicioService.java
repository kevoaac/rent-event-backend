package com.rentevent.service;

import com.rentevent.model.servicio.Servicio;
import com.rentevent.model.servicio.ServicioDTO;
import com.rentevent.model.servicio.ServicioRequest;
import com.rentevent.model.servicio.ServicioResponse;
import com.rentevent.repository.IServicioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
// Lombok genera un constructor con los atributos marcados como final, hace que no sea necesario inyectar las dependencias con @Autowired
public class ServicioService {
    private final IServicioRepository servicioRepository;

    @Transactional
    public ServicioResponse updateService(ServicioRequest servicioRequest) {

        Servicio servicio = Servicio.builder()
                .codigo(servicioRequest.getCodigo())
                .nombre(servicioRequest.getNombre())
                .tipo(servicioRequest.getTipo())
                .costo(servicioRequest.getPrecio())
                .descripcion(servicioRequest.getDescripcion())
                .descripcion(servicioRequest.getPersonalizacion())
                .imagen(servicioRequest.getImagen())
                .build();


        // servicioRepository.updateUser(servicio.id, servicio.firstname, servicio.lastname, servicio.country);
        servicioRepository.updateServicio(
                servicio.getNombre(), servicio.getTipo(), servicio.getCosto(), servicio.getDescripcion(),
                servicio.getDescripcion(), servicio.getImagen(), servicio.getCodigo());

        return new ServicioResponse("El servicio se registró satisfactoriamente");
    }

    public ServicioDTO getService(String codigo) {
        Servicio servicio = servicioRepository.findByCodigo(codigo).orElse(null);

        if (servicio != null) {
            ServicioDTO servicioDTO = ServicioDTO.builder()
                    .codigo(servicio.getCodigo())
                    .nombre(servicio.getNombre())
                    .tipo(servicio.getTipo())
                    .precio(servicio.getCosto())
                    .descripcion(servicio.getDescripcion())
                    .personalizacion(servicio.getDescripcion())
                    .imagen(servicio.getImagen())
                    .build();
            return servicioDTO;
        }
        return null;
    }
}
