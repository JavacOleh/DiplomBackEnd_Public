package diplom.service;

import diplom.model.entity.rider.Rider;
import diplom.repository.RiderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RiderService {
    public final RiderRepository riderRepository;

    @Autowired
    public RiderService(RiderRepository riderRepository) {
        this.riderRepository = riderRepository;
    }

    public boolean addRider(Rider rider) {
        var update = rider != null && !riderRepository.existsByEmail(rider.getEmail());

        if(update) {
            riderRepository.save(rider);
        }

        return update;
    }

    public boolean updateRiderDate(Rider rider) {
        var update = rider != null;

        if (update) {
            var temp = riderRepository.findById(rider.getId());

            if (temp.isPresent()) {
                Rider existingRider = temp.get();
                existingRider.setFirstName(rider.getFirstName());
                existingRider.setLastName(rider.getLastName());
                existingRider.setEmail(rider.getEmail());
                existingRider.setPhoneNumber(rider.getPhoneNumber());
                existingRider.setAddress(rider.getAddress());
                existingRider.setCarType(rider.getCarType());  // Обновляем все поля, которые пришли в запросе

                riderRepository.save(existingRider);  // Сохраняем обновленного райдера
            }
        }

        return update;
    }

    public boolean deleteRider(Long id) {
        var rider = riderRepository.findById(id);
        boolean update = rider.isPresent();

        if(update) {
            riderRepository.deleteById(id);
        }

        return update;
    }
    public Rider getRider(long id) {
        var rider = riderRepository.findById(id);
        var update = rider.isPresent();

        return update ? rider.get() : null;
    }
}
