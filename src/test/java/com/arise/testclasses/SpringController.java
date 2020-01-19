package com.arise.testclasses;

import com.arise.testclasses.TestDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

@RequestMapping("/api")
public class SpringController {

//    @GetMapping("uri/locations")
//    public void getLocations(){
//
//    }

    @GetMapping(value = "/uri/locations/list", consumes = {MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public void getLocations(@RequestBody  Set set, @RequestBody  Set set1){

    }

//    @GetMapping("uri/location")
//    public void get(@RequestBody TestDTO.InnerDto dto, @RequestBody SimpleDTO innerDto){
//
//    }
//
//    @PostMapping("/uri/post/{arg1}/{arg2}/{arg3}")
//    public void post(
//            @PathVariable("arg1") String arg1,
//            @PathVariable("arg2") String arg2,
//            @PathVariable Integer arg3,
//            @PathVariable Long dasdasd,
//            @PathVariable float asdasdasd,
//            @PathVariable boolean asdasdasdassd,
//            @RequestParam String stuff,
//            @RequestHeader String header
//
//    ){
//
//    }
//
//    @DeleteMapping
//    public void delete(){
//
//    }
//
//    @GetMapping("/test/query")
//    public ResponseEntity<TestDTO> getDTO(@RequestBody TestDTO requestBody,
//                                          @RequestParam("queryParameter") String s){
//        return null;
//    }
//
//    @GetMapping
//    public ResponseEntity<Set<TestDTO>> getDTOSet(){
//        return null;
//    }
//
//    @GetMapping
//    public ResponseEntity<Map<String, TestDTO>> getDTOMap(){
//        return null;
//    }
//
//    @PatchMapping
//    public ResponseEntity<Map<String, TestDTO>> patchDTOMap(){
//        return null;
//    }
//
////    @RequestMapping(name = "GET", path = {"/index/{path}", "/index2/{path}"})
////    public ResponseEntity<Map<String, TestDTO>> reguestMappingGet2Paths(){
////        return null;
////    }
//
//    @RequestMapping(name = "GET", path = {"/index/{g1}", "/index1/{g2}"}, method = {RequestMethod.HEAD})
//    public ResponseEntity<Map<String, TestDTO>> reguestMappingGet2Paths2Methods(
//            @RequestBody  Set<TestDTO> set,
//            @PathVariable("g1") String g1Java,
//            @PathVariable("g2") String g2Java,
//            @RequestHeader(name = "Some-Header", defaultValue = "dasdasdasd", required = true) String header
//    ){
//        return null;
//    }
//
//    @GetMapping("/get/parametrized/body")
//    public ResponseEntity<ResponseEntity<Map<Integer, Map<Integer, TestDTO>>>> getDTOParametrizedType(
//           @RequestBody ResponseEntity<ResponseEntity<Map<String, Map<String, TestDTO>>>> arg
//    ){
//        return null;
//    }
//
//    @RequestMapping(name = "GET",
//            path = {"/index/{g0}", "/index1/{g1}"},
//            method = {RequestMethod.HEAD},
//            value = {"index4/url"})
//    public ResponseEntity<Map<String, TestDTO>> reguestMappingGet2Paths2Methodsdasdasd(){
//        return null;
//    }
}
