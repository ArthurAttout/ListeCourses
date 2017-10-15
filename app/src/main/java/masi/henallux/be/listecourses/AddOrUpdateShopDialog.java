package masi.henallux.be.listecourses;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.List;
import java.util.UUID;

import masi.henallux.be.listecourses.model.Address;
import masi.henallux.be.listecourses.model.Shop;
import masi.henallux.be.listecourses.utils.Constants;
import masi.henallux.be.listecourses.utils.FileService;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Arthur on 12-10-17.
 */

public class AddOrUpdateShopDialog extends DialogFragment implements Validator.ValidationListener {

    private AddShopCallback callback;

    @NotEmpty(messageResId = R.string.error_empty_field)
    private EditText editTextName;
    @NotEmpty(messageResId = R.string.error_empty_field)
    private EditText editTextStreetNumber;
    @NotEmpty(messageResId = R.string.error_empty_field)
    private EditText editTextStreetName;
    @NotEmpty(messageResId = R.string.error_empty_field)
    private EditText editTextCity;
    @NotEmpty(messageResId = R.string.error_empty_field)
    private EditText editTextZipCode;

    private boolean editMode;
    private Shop shopToUpdate;
    private Validator validator;
    private ImageButton addPictureButton;
    private Uri selectedImageUri;

    private FileService fileService;

    @Override //todo replace deprecated method
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            callback = (AddShopCallback) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Unrecognized callback type",e);
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        fileService = new FileService(getActivity());

        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_add_shop, null);

        editTextName = (EditText)view.findViewById(R.id.editTextNameShop);
        editTextStreetNumber = (EditText)view.findViewById(R.id.editTextStreetNumber);
        editTextStreetName = (EditText)view.findViewById(R.id.editTextStreetName);
        editTextCity = (EditText)view.findViewById(R.id.editTextCity);
        editTextZipCode = (EditText)view.findViewById(R.id.editTextZipCode);
        addPictureButton = (ImageButton)view.findViewById(R.id.imageButtonAddPicture);

        validator = new Validator(this);
        validator.setValidationListener(this);

        Bundle arguments = getArguments();
        if(arguments != null) {
            shopToUpdate = arguments.getParcelable(Constants.BUNDLE_SHOP_TO_UPDATE);
            if (shopToUpdate != null) {
                editTextName.setText(shopToUpdate.getName());
                editTextStreetNumber.setText(shopToUpdate.getAddress().getStreetNumber());
                editTextStreetName.setText(shopToUpdate.getAddress().getStreetName());
                editTextCity.setText(shopToUpdate.getAddress().getCity());
                editTextZipCode.setText(shopToUpdate.getAddress().getPostalCode());
            }
        }

        addPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), Constants.REQUESTCODE_GALLERY_PICTURE);
            }
        });

        editMode = !(shopToUpdate == null);
        builder.setView(view)
                .setPositiveButton((editMode ? R.string.update_action : R.string.add_action), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {} //Trick to avoid the DialogFragment to dismiss when any button is pressed (validation needed)
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddOrUpdateShopDialog.this.getDialog().cancel();
                    }
        });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    validator.validate();
                }
            });
        }
    }

    @Override
    public void onValidationSucceeded() {
        if(editMode){
            shopToUpdate.setName(editTextName.getText().toString());
            shopToUpdate.getAddress().setStreetNumber(editTextStreetNumber.getText().toString());
            shopToUpdate.getAddress().setStreetName(editTextStreetName.getText().toString());
            shopToUpdate.getAddress().setCity(editTextCity.getText().toString());
            shopToUpdate.getAddress().setPostalCode(editTextZipCode.getText().toString());
            shopToUpdate.setShopMapUriDrawable(null); //Invalidate image, since the address may have changed
            shopToUpdate.setShopLogoUriDrawable(selectedImageUri);
            callback.updateShopCallback(shopToUpdate);
        }
        else
        {
            Shop shop = new Shop();
            Address address = new Address();

            shop.setName(editTextName.getText().toString());
            address.setStreetNumber(editTextStreetNumber.getText().toString());
            address.setStreetName(editTextStreetName.getText().toString());
            address.setCity(editTextCity.getText().toString());
            address.setPostalCode(editTextZipCode.getText().toString());
            shop.setShopLogoUriDrawable(selectedImageUri);
            address.setCountry("Belgique"); //Todo allow other countries
            shop.setAddress(address);
            callback.addShopCallback(shop);
        }
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this.getActivity());

            // Display error messages
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUESTCODE_GALLERY_PICTURE) {
                Uri tempUri = data.getData();
                if(tempUri == null){
                    Toast.makeText(getActivity(), R.string.error_couldnt_get_picture, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    selectedImageUri = fileService.saveUri(tempUri,"Pic_" + UUID.randomUUID().toString().substring(0,3)); //Todo retrieve last autoincrement from repository instead of UUID
                }
            }
        }
    }

    public interface AddShopCallback{
        void addShopCallback(Shop newShop);
        void updateShopCallback(Shop updatedShop);
    }
}
