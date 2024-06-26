package com.rentevent.service;

import com.rentevent.dto.request.ServicioRequest;
import com.rentevent.dto.response.CloudinaryResponse;
import com.rentevent.exception.NotFoundException;
import com.rentevent.model.imagen.Imagen;
import com.rentevent.model.proveedor.Proveedor;
import com.rentevent.model.servicio.Servicio;
import com.rentevent.model.servicio.ServicioResponse;
import com.rentevent.repository.IImagenRepository;
import com.rentevent.repository.IProveedorRepository;
import com.rentevent.repository.IServicioRepository;
import com.rentevent.util.FileUploadUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
// Lombok genera un constructor con los atributos marcados como final, hace que no sea necesario inyectar las dependencias con @Autowired
public class ServicioService {
    private final IServicioRepository servicioRepository;
    private final IProveedorRepository proveedorRepository;
    private final IImagenRepository imagenRepository;
    @Autowired
    private CloudinaryService cloudinaryService;

    public ServicioResponse obtenerServicioPorCodigo(String codigo) {
        Servicio servicio = servicioRepository.findByCodigo(codigo).orElse(null);

        if (servicio != null) {
            ServicioResponse servicioResponse = ServicioResponse.builder()
                    .codigo(servicio.getCodigo())
                    .nombre(servicio.getNombre())
                    .tipo(servicio.getTipo())
                    .costo(servicio.getCosto())
                    .estado(servicio.getEstado().toString())
                    .descripcion(servicio.getDescripcion())
                    .imagenes(servicio.getImagenes())
                    .proveedor(servicio.getProveedor())
                    .build();
            return servicioResponse;
        }
        return null;
    }

    public List<Servicio> obtenerServicios() {
        return this.servicioRepository.findAll();
    }

    public ServicioResponse obtenerServicioPorId(Integer id) {
        Servicio servicio = this.servicioRepository.findById(id).orElseThrow(() -> new NotFoundException("Servicio no encontrado"));
        return ServicioResponse.builder()
                .codigo(servicio.getCodigo())
                .nombre(servicio.getNombre())
                .tipo(servicio.getTipo())
                .costo(servicio.getCosto())
                .descripcion(servicio.getDescripcion())
                .imagenes(servicio.getImagenes())
                .eventos(servicio.getEventos())
                .proveedor(servicio.getProveedor())
                .build();
    }

    @Transactional
    public void guardarServicio(ServicioRequest servicioRequest, MultipartFile file) {
        UUID uuid = UUID.randomUUID();
        System.out.println(uuid.toString());

        Proveedor proveedor = this.proveedorRepository.findByNombre(servicioRequest.getProveedor()).orElseThrow(() -> new NotFoundException("Proveedor no encontrado"));

        String fileName = getFileName(file);
        final CloudinaryResponse response = this.cloudinaryService.uploadFile(file, fileName);

        Imagen imagen = Imagen.builder()
                .url(response.getUrl())
                .nombre(fileName)
                .idPublica(response.getPublicId())
                .etiqueta("SERVICIO")
                .build();

        Servicio servicio = Servicio.builder()
                .codigo("SERV-" + uuid.toString().substring(0, 8))
                .nombre(servicioRequest.getNombre())
                .tipo(servicioRequest.getTipo())
                .costo(servicioRequest.getCosto())
                .estado(servicioRequest.getEstado())
                .descripcion(servicioRequest.getDescripcion())
                .proveedor(proveedor)
                .imagenes(List.of(imagen))
                .build();

        imagen.setServicio(servicio);
        proveedor.getServicios().add(servicio);

        this.servicioRepository.save(servicio);
    }

    @Transactional
    public void actualizarServicio(String codigo, ServicioRequest servicioRequest, MultipartFile file) {
        Servicio servicio = this.servicioRepository.findByCodigo(codigo).orElseThrow(() -> new NotFoundException("Servicio no encontrado"));
        System.out.println("Servicio: " + servicio.getNombre());
        Proveedor proveedor = this.proveedorRepository.findByNombre(servicioRequest.getProveedor()).orElseThrow(() -> new NotFoundException("Proveedor no encontrado"));
        System.out.println("Proveedor: " + proveedor.getNombre());

        String originalFileName = file.getOriginalFilename();

        System.out.println("File NAME: " + originalFileName);
        if (originalFileName.equals("blob")) {

            servicio.setNombre(servicioRequest.getNombre());
            servicio.setTipo(servicioRequest.getTipo());
            servicio.setCosto(servicioRequest.getCosto());
            servicio.setEstado(servicioRequest.getEstado());
            servicio.setDescripcion(servicioRequest.getDescripcion());
            servicio.setProveedor(proveedor);

            this.servicioRepository.save(servicio);

            return;
        }
        String fileName = getFileName(file);

        // Eliminar imagen anterior de Cloudinary
        String idPublica = servicio.getImagenes().get(0).getIdPublica();
        servicio.getImagenes().clear();

        this.imagenRepository.deleteByIdPublica(idPublica);
        this.cloudinaryService.deleteFile(idPublica);

        CloudinaryResponse responseCloudinary = this.cloudinaryService.uploadFile(file, fileName);

        Imagen imagen = Imagen.builder()
                .url(responseCloudinary.getUrl())
                .nombre(fileName)
                .idPublica(responseCloudinary.getPublicId())
                .etiqueta("SERVICIO")
                .build();

        System.out.println("Imagen: " + imagen);
        servicio.setNombre(servicioRequest.getNombre());
        servicio.setTipo(servicioRequest.getTipo());
        servicio.setCosto(servicioRequest.getCosto());
        servicio.setEstado(servicioRequest.getEstado());
        servicio.setDescripcion(servicioRequest.getDescripcion());
        servicio.setProveedor(proveedor);
        servicio.getImagenes().add(imagen);

        imagen.setServicio(servicio);

        this.servicioRepository.save(servicio);

    }

    public String getFileName(MultipartFile file) {
        FileUploadUtil.assertAllowed(file, FileUploadUtil.IMAGE_PATTERN);
        return FileUploadUtil.getFileName(file.getOriginalFilename());
    }

    @Transactional
    public void subirImagenParaServicio(final Integer idServicio, final MultipartFile file) {
        final Servicio servicio = this.servicioRepository.findById(idServicio).orElseThrow(() -> new NotFoundException("Servicio no encontrado"));
        if (servicio == null) {
            throw new RuntimeException("Servicio no encontrado");
        }

        FileUploadUtil.assertAllowed(file, FileUploadUtil.IMAGE_PATTERN);
        final String fileName = FileUploadUtil.getFileName(file.getOriginalFilename());
        final CloudinaryResponse response = this.cloudinaryService.uploadFile(file, fileName);

        Imagen imagen = Imagen.builder()
                .url(response.getUrl())
                .nombre(fileName)
                .idPublica(response.getPublicId())
                .servicio(servicio)
                .etiqueta("servicio")
                .build();

        servicio.getImagenes().add(imagen);

        this.servicioRepository.save(servicio);
    }

}
