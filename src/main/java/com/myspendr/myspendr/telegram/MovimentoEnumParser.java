package com.myspendr.myspendr.telegram;

import com.myspendr.myspendr.model.CategoriaMovimento;
import com.myspendr.myspendr.model.TipoMovimento;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class MovimentoEnumParser {

    public static Optional<TipoMovimento> parseTipo(String input) {
        return Arrays.stream(TipoMovimento.values())
                .filter(t -> t.name().equalsIgnoreCase(input.trim()))
                .findFirst();
    }

    public static Optional<CategoriaMovimento> parseCategoria(String input) {
        return Arrays.stream(CategoriaMovimento.values())
                .filter(c -> c.name().equalsIgnoreCase(input.trim()))
                .findFirst();
    }

    public static String getCategorieValide() {
        return Arrays.stream(CategoriaMovimento.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}

