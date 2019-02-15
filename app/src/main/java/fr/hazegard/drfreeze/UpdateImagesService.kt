package fr.hazegard.drfreeze

import javax.inject.Inject

class UpdateImagesService {

    @Inject
    lateinit var imageManager: ImageManager

    @Inject
    lateinit var packageManager: PackageManager

    fun updateImages() {
        imageManager.updateImages(packageManager.getTrackedPackages())
    }
}