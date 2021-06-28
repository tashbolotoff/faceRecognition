package kg.socservice.faceRecognition.models;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FaceModel {
    String snapPicURL;
    String snapTime;
    String facePicURL;
    String FDname;
    String bornTime;
    String name;
    String picURL;
    String similarity;
}
