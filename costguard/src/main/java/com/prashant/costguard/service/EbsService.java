package com.prashant.costguard.service;

import com.prashant.costguard.model.EC2Instance;
import com.prashant.costguard.model.EbsVolume;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeVolumesResponse;
import software.amazon.awssdk.services.ec2.model.Volume;

import java.util.List;
import java.util.ArrayList;

@Service
public class EbsService {

    private final Ec2Client ec2Client;

    public EbsService(){
        this.ec2Client = Ec2Client.create();
    }

    public List<EbsVolume> getAllVolume(){



    }


}
