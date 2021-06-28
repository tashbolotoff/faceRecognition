package kg.socservice.faceRecognition.controllers;

import kg.socservice.faceRecognition.services.FaceRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @Autowired
    private FaceRecognitionService faceRecognitionService;

    @GetMapping("/")
    public String getUserList(Model model) {
        model.addAttribute("newFaces",faceRecognitionService.faceIsapi());
        return "face/faces_list";
    }
}
