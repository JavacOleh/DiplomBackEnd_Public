package diplom.controller;


import diplom.model.entity.rider.Rider;
import diplom.service.RiderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/riders")
public class RiderController {
    private final RiderService riderService;

    @Autowired
    public RiderController(RiderService riderService) {
        this.riderService = riderService;
    }

    @GetMapping()
    public List<Rider> getRiders() {
        return riderService.riderRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rider> getRider(@PathVariable Long id) {
        var rider = riderService.getRider(id);

        return rider == null
                ? ResponseEntity.badRequest().build()
                : ResponseEntity.ok(rider);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addRider(@Valid @RequestBody Rider rider, BindingResult result) {

        if (result.hasErrors()) {
            List<String> errorMessages = result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();

            return ResponseEntity.badRequest().body(errorMessages.toString());
        }

        var updated = riderService.addRider(rider);

        return updated
                ? ResponseEntity.ok().body("ok")
                : ResponseEntity.badRequest().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateRider(@RequestBody Rider rider) {
        var updated = riderService.updateRiderDate(rider);

        return updated
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRider(@PathVariable long id) {
        var rider = riderService.getRider(id);

        if(rider != null) {
            riderService.deleteRider(id);
        }

        return rider == null
                ? ResponseEntity.badRequest().build()
                : ResponseEntity.ok().build();
    }
}
