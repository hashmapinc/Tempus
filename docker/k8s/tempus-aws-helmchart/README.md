# Instructions for AWS + Rancher
Rancher and AWS will work together, but it's poorly documented...

## Step 1 - Create AWS Policy
Ensure that the following policy exists in AWS IAM:

**Name**: `rancher-aws`

**JSON**:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "ec2:Describe*",
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": "ec2:AttachVolume",
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": "ec2:DetachVolume",
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ec2:*"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "elasticloadbalancing:*"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "iam:PassRole",
        "iam:ListInstanceProfiles",
        "ec2:*"
      ],
      "Resource": "*"
    }
  ]
}
```

## Step 2 - Create AWS Role
Ensure that the following role exists in AWS IAM:

**Name**: `rancher-instance-role`

Make sure to attach the AWS Policy you just created to this role.

## Step 3 - Create AWS User
Create the following user in AWS IAM:

**Name**: `rancher`
 
Make sure to attach the AWS Policy you just created to this user.

This is the user that will be used in rancher to connect to AWS. Take note of the `Access Key` and `Secret Key` for this user.

## Step 4 - Create AWS EC2 Security Group (if it doesn't exist!)
Create the following EC2 security group in AWS:

**Name**: `rancher-hosts`
 
The security group should have the following inbound traffic policies:


|      Type       | Protocol | Port Range |  Source   |
| --------------- | -------- | ---------- | --------- |
| Custom TCP Rule | TCP      | 8080       | 0.0.0.0/0 |
| Custom TCP Rule | TCP      | 9191       | 0.0.0.0/0 |
| Custom TCP Rule | TCP      | 44134      | 0.0.0.0/0 |
| Custom TCP Rule | TCP      | 44134      | ::/0      |
| SSH             | TCP      | 22         | 0.0.0.0/0 |
| Custom TCP Rule | TCP      | 2376       | 0.0.0.0/0 |
| Custom TCP Rule | TCP      | 10250      | 0.0.0.0/0 |
| Custom TCP Rule | TCP      | 10250      | ::/0      |
| Custom TCP Rule | TCP      | 9090       | 0.0.0.0/0 |
| Custom UDP Rule | UDP      | 4500       | 0.0.0.0/0 |
| Custom UDP Rule | UDP      | 500        | 0.0.0.0/0 |
| Custom TCP Rule | TCP      | 1883       | 0.0.0.0/0 |


## Step 5 - Create AWS Hosts in Rancher
In your rancher environment, add a host and select Amazon EC2 as the host type.

Enter the `Access Key` and `Secret Key` for the `rancher` user.

For the `Security Group`, choose select the Custom option and in the drop down, pick `rancher-hosts` as the security group.

Continue with the setup as usual until you're able to name the host. At this point, you should see a feild called `IAM Profile`. Enter the name of the AWS Role you created above for this value.

Finish the host creation process as usual.

## Step 6 - Update Rancher Kubernetes Cloud Provider:
In rancher, go to the Kubernetes tab and select Infrastructure Stack.

Click the `up to date` section of the `kubernetes` entry. If you do not see a configuration screen, select a template version and proceed.

Find the `Cloud Provider` dropdown and change it from `rancher` to `aws`.

From there, select Upgrade and allow the kubernetes changes to take effect.

If you have issues, you may have to delete and recreate the hosts after this step.

## Note on Storage Class YAMLs
Ensure that your rancher hosts and the EBS Storage Classes you define share the same Availability Zone, or you will get errors. In this case, the zone is `us-east-2a`. This is defined in the `common.yaml` file.

## Help
If you have any issues, please reach out to [Randy Pitcher](https://github.com/randypitcherii)
