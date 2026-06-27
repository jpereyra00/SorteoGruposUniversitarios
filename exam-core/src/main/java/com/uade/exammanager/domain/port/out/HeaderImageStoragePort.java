package com.uade.exammanager.domain.port.out;

/**
 * Puerto de salida para almacenar la imagen de cabecera del examen.
 * No depende de tipos web; recibe los bytes ya leídos.
 */
public interface HeaderImageStoragePort {

    /**
     * Almacena la imagen y devuelve la ruta absoluta del archivo guardado.
     *
     * @param content          contenido binario de la imagen
     * @param originalFilename nombre original (para inferir la extensión)
     */
    String store(byte[] content, String originalFilename);
}
