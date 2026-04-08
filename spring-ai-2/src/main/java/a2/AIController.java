//package a2;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class AIController {
//
////   @Resource(name = "chatModelMap")
////   private AIModelConfig.ModelMap chatModelMap;
////
////    @GetMapping ("/chat")
////    public String chat(@RequestParam("message") String message) {
////        return callModel(message, "qianwen");
////    }
////
////    @GetMapping ("/localChat")
////    public String localChat(@RequestParam("message") String message) {
////        return callModel(message, "localQianwen");
////    }
//
////    private String callModel(String message, String qianwen) {
////        if (qianwen == null) {
////            chatModelMap.get("qianwen").call(message);
////        }
////        return chatModelMap.get(qianwen).call(message);
////    }
////
////    private Flux<String> streamCallModel(String message, String qianwen) {
////        if (qianwen == null) {
////            chatModelMap.get("qianwen").call(message);
////        }
////        return chatModelMap.get(qianwen).stream(message);
////    }
//
//
////    @GetMapping("/stream/chat")
////    public Flux<String> streamChat(@RequestParam("message") String message) {
////        return streamCallModel(message, null);
////    }
//
//    @GetMapping("/stream/chat")
//    public String streamChat(String message) {
//        return message;
//    }
//}
