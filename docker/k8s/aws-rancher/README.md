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

## Step 4 - Create AWS Hosts in Rancher
In your rancher environment, add a host and select Amazon EC2 as the host type.

Enter the `Access Key` and `Secret Key` for the `rancher` user.

Continue with the setup as usual until you're able to name the host. At this point, you should see a feild called `IAM Profile`. Enter the name of the AWS Role you created above for this value.

Finish the host creation process as usual.

## Step 5 - Update Rancher Kubernetes Cloud Provider:
In rancher, go to the Kubernetes tab and select Infrastructure Stack.

Click the `up to date` section of the `kubernetes` entry. If you do not see a configuration screen, select a template version and proceed.

Find the `Cloud Provider` dropdown and change it from `rancher` to `aws`.

From there, select Upgrade and allow the kubernetes changes to take effect.

If you have issues, you may have to delete and recreate the hosts after this step.

## Help
If you have any issues, please reach out to [Randy Pitcher](https://github.com/randypitcherii)
