package com.crimsonhexagon.mockingbird;

import java.io.File;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.System.exit;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class Launch {

    public static void main(String[] args) {
	    Stream<File> files = stream(args).map(File::new);
	    List<File> missing = files.filter(((Predicate<? super File>) File::canRead).negate()).collect(toList());
	    if (!missing.isEmpty()) {
		    missing.forEach(f -> System.out.println(format("Missing file \"%s\".", f.getAbsolutePath())));
		    exit(1);
	    }

	    new Mockingbird()
			    .generate(stream(args).map(File::new).toArray(File[]::new))
			    .forEach(System.out::println);
    }
}
