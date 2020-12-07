package org.folio.edge.rtac.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.folio.edge.core.Constants;


public enum RtacMimeTypeEnum {
  ALL("*/*"),
  APPLICATION_XML(Constants.APPLICATION_XML),
  TEXT_XML(Constants.TEXT_XML),
  APPLICATION_JSON(Constants.APPLICATION_JSON);

  private static final List<RtacMimeTypeEnum> mimeTypes = new ArrayList<>();
  private final String mimeType;

  static {
    Arrays.stream(RtacMimeTypeEnum.values()).forEach(e -> mimeTypes.add(e));
  }

  RtacMimeTypeEnum(String mimeType) {
    this.mimeType = mimeType;
  }

  @Override
  public String toString() {
    return mimeType;
  }

  public static RtacMimeTypeEnum[] getAllTypes() {
    return mimeTypes.toArray(new RtacMimeTypeEnum[0]);
  }

  public static String[] getAllTypesAsString() {
    return Arrays.stream(mimeTypes.toArray(new RtacMimeTypeEnum[0])).map(RtacMimeTypeEnum::toString).toArray(String[]::new);
  }

}